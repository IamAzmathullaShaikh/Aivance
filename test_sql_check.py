"""Mechanical validation of every seed INSERT / scalar query in MigrationTest.kt
against the exported Room schemas. Builds the base DB at each seed version with
PRAGMA foreign_keys=ON and executes the exact SQL from the Kotlin test file.
"""
import json, re, sqlite3, sys

BASE = 'core/database/schemas/com.bangersoul.aivance.core.database.AivanceDatabase'
KOTLIN_TEST = 'core/database/src/androidTest/java/com/bangersoul/aivance/core/database/MigrationTest.kt'

def load_db(v):
    with open(f'{BASE}/{v}.json') as f:
        return json.load(f)['database']

def build_base(v, con):
    for e in load_db(v)['entities']:
        sql = e['createSql'].replace('${TABLE_NAME}', e['tableName'])
        con.execute(sql)
        for idx in (e.get('indices') or []):
            cols = ', '.join('`%s`' % c for c in idx['columnNames'])
            uniq = ' UNIQUE' if idx.get('unique') else ''
            con.execute(f"CREATE{uniq} INDEX IF NOT EXISTS `{idx['name']}` ON `{e['tableName']}` ({cols})")

src = open(KOTLIN_TEST, encoding='utf-8').read()

def unescape_kt(s):
    out = []
    i = 0
    while i < len(s):
        if s[i] == '\\' and i + 1 < len(s):
            nxt = s[i + 1]
            if nxt == '\\': out.append('\\'); i += 2; continue
            if nxt == '"': out.append('"'); i += 2; continue
            if nxt == 'n': out.append('\n'); i += 2; continue
            if nxt == 't': out.append('\t'); i += 2; continue
            if nxt == '$': out.append('$'); i += 2; continue
            out.append(nxt); i += 2; continue
        out.append(s[i]); i += 1
    return ''.join(out)

def split_top_level(s, sep=','):
    """Split on sep that is not inside quotes, parens, or brackets."""
    parts = []
    depth = 0
    cur = []
    in_str = False
    i = 0
    while i < len(s):
        c = s[i]
        if c == '"':
            if not in_str:
                in_str = True
            else:
                # check for escaped quote
                back = 0
                j = i - 1
                while j >= 0 and s[j] == '\\':
                    back += 1; j -= 1
                if back % 2 == 0:
                    in_str = False
        if not in_str:
            if c in '([':
                depth += 1
            elif c in ')]':
                depth -= 1
            elif c == sep and depth == 0:
                parts.append(''.join(cur)); cur = []; i += 1; continue
        cur.append(c)
        i += 1
    parts.append(''.join(cur))
    return parts

def string_literals(s):
    return [unescape_kt(q.group(1)) for q in re.finditer(r'"((?:[^"\\]|\\.)*)"', s)]

def matching_paren(s, start):
    depth = 0
    i = start
    in_str = False
    while i < len(s):
        c = s[i]
        if c == '"':
            if not in_str:
                in_str = True
            else:
                back = 0
                j = i - 1
                while j >= 0 and s[j] == '\\':
                    back += 1; j -= 1
                if back % 2 == 0:
                    in_str = False
            i += 1; continue
        if in_str:
            i += 1; continue
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1

results = []
for m in re.finditer(r'\bseed\(', src):
    close = matching_paren(src, m.end() - 1)
    args_src = src[m.end():close]
    label = src[max(0, src.rfind('fun ', 0, m.start())):m.start()].split('fun ')[-1].strip()[:44]
    args = split_top_level(args_src)
    if not args or not re.match(r'\s*\d+\s*$', args[0]):
        results.append((None, [], label, args_src))
        continue
    version = int(args[0])
    stmts = []
    for a in args[1:]:
        literals = string_literals(a)
        if literals:
            stmts.append(''.join(literals))
    results.append((version, stmts, label, args_src))

fails = 0
print('seed() statement validation (base schema, FK ON):')
for version, stmts, label, _ in results:
    con = sqlite3.connect(':memory:')
    con.execute('PRAGMA foreign_keys=ON')
    if version is None:
        print(f'  [SKIP] {label}: seed(expression or empty)')
        con.close()
        continue
    if any('$' in s for s in stmts):
        print(f'  [SKIP] {label}: interpolated stress seed (replicated separately)')
        con.close()
        continue
    build_base(version, con)
    errs = []
    for stmt in stmts:
        try:
            con.execute(stmt)
        except Exception as e:
            errs.append(f'{stmt[:90]}... -> {e}')
    if errs:
        print(f'  [FAIL] {label} (seed v{version})')
        for e in errs:
            print(f'      - {e}')
        fails += 1
    else:
        print(f'  [OK]   {label} (seed v{version}, {len(stmts)} stmt)')
    con.close()

# ---- scalar() queries executed against a full v24 DB ----
print()
print('scalar() queries against full v24 DB:')
con = sqlite3.connect(':memory:')
con.execute('PRAGMA foreign_keys=ON')
build_base(24, con)
seed_order = [
    "INSERT INTO user_profiles (id, name, email, skills, experienceYears, createdDate, preferredIndustries, visaRequired) VALUES ('u1','Alice','a@x.com','[]',3,5,'[]',0)",
    "INSERT INTO companies (id, name) VALUES (1,'Acme')",
    "INSERT INTO resumes (id, name, rawText, dateCreated, lastModified) VALUES (1,'My Resume','body text',100,200)",
    "INSERT INTO resume_versions (id, resumeId, versionName, templateId, lastModified) VALUES (1,1,'Main','modern',2)",
    "INSERT INTO jobs (id, companyId, title, url, sourceProviderId, postedDate) VALUES (1,1,'Eng','','X',100)",
    "INSERT INTO cover_letters (id, company, role, dateCreated) VALUES (1,'Acme','Eng',100)",
    "INSERT INTO provider_configurations (provider, type, settings, isEnabled) VALUES ('claude','AI','{}',1)",
    "INSERT INTO interview_questions (text, category, difficulty, isFavorite) VALUES ('Q?','TECH','MEDIUM',0)",
    "INSERT INTO job_applications (id, jobId, status, dateApplied, lastModified) VALUES (1,1,'APPLIED',100,100)",
    "INSERT INTO applications (id, jobId, status, currentStageId, dateApplied, lastModified) VALUES (1,1,'ACTIVE','APPLIED',100,100)",
    "INSERT INTO application_timeline (id, applicationId, eventType, title, timestamp) VALUES (1,1,'EVENT','E',100)",
    "INSERT INTO interview_sessions (id, targetRole, type, difficulty, dateStarted, isCompleted) VALUES (1,'Eng','BEHAVIORAL','MEDIUM',100,0)",
]
for s in seed_order:
    con.execute(s)
# migrate9To10 asserts apiKey against the *v10* schema (apiKey is dropped only at 19->20),
# so it legitimately fails against a v24 DB — excluded here, validated by the v10 replay.
VERSION_SPECIFIC = ['SELECT apiKey FROM provider_configurations']
for q in re.finditer(r'scalar\(\s*"((?:[^"\\]|\\.)*)"\s*\)', src):
    sql = unescape_kt(q.group(1))
    if any(sql.startswith(p) for p in VERSION_SPECIFIC):
        print(f'  [SKIP] {sql[:90]} (v10-specific, verified in migration replay)')
        continue
    try:
        cur = con.execute(sql)
        cur.fetchone()
        print(f'  [OK]   {sql[:90]}')
    except Exception as e:
        print(f'  [FAIL] {sql[:90]} -> {e}')
        fails += 1
con.close()

# ---- stress test: replicate the generated SQL in Python and run on v17 ----
print()
print('stress test (17 -> 24) seed replication:')
con = sqlite3.connect(':memory:')
con.execute('PRAGMA foreign_keys=ON')
build_base(17, con)
companies = ','.join(f"({i}, 'C{i}')" for i in range(1, 101))
jobs = ','.join(f"({i}, {(i % 100) + 1}, 'J{i}', '', 'PROVIDER', {i * 10})" for i in range(1, 1001))
recruiters = ','.join(f"('r{i}', {(i % 100) + 1}, 'Rec{i}', 'ACTIVE')" for i in range(1, 501))
resumes = ','.join(f"({i}, 'Res{i}', {i * 10}, {i * 10 + 1})" for i in range(1, 101))
resume_versions = ','.join(f"({i}, {(i % 100) + 1}, 'Main', 'modern', {i * 10})" for i in range(1, 101))
cover_letters = ','.join(f"({i}, {(i % 100) + 1}, {(i % 1000) + 1}, 'CL{i}', 'Eng', {i * 10})" for i in range(1, 101))
applications = ','.join(f"({i}, {(i % 1000) + 1}, 'ACTIVE', 'APPLIED', {i * 10}, {i * 10})" for i in range(1, 501))
sessions = ','.join(f"({i}, 'Eng{i}', 'BEHAVIORAL', 'MEDIUM', {i * 10}, {1 if i % 2 == 0 else 0})" for i in range(1, 101))
timeline = ','.join(f"({i}, {(i % 500) + 1}, 'EVENT', 'E{i}', {i * 10})" for i in range(1, 1001))
stress_sql = [
    f"INSERT INTO companies (id, name) VALUES {companies}",
    f"INSERT INTO jobs (id, companyId, title, url, sourceProviderId, postedDate) VALUES {jobs}",
    f"INSERT INTO recruiters (id, companyId, name, status) VALUES {recruiters}",
    f"INSERT INTO resumes (id, name, dateCreated, lastModified) VALUES {resumes}",
    f"INSERT INTO resume_versions (id, resumeId, versionName, templateId, lastModified) VALUES {resume_versions}",
    f"INSERT INTO cover_letters (id, resumeVersionId, jobId, company, role, dateCreated) VALUES {cover_letters}",
    f"INSERT INTO applications (id, jobId, status, currentStageId, dateApplied, lastModified) VALUES {applications}",
    f"INSERT INTO interview_sessions (id, targetRole, type, difficulty, dateStarted, isCompleted) VALUES {sessions}",
    f"INSERT INTO application_timeline (id, applicationId, eventType, title, timestamp) VALUES {timeline}",
]
for s in stress_sql:
    try:
        con.execute(s)
    except Exception as e:
        print(f'  [FAIL] {s[:70]}... -> {e}')
        fails += 1
        break
else:
    n = 0
    for r in con.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'"):
        n += con.execute(f'SELECT COUNT(*) FROM "{r[0]}"').fetchone()[0]
    print(f'  [OK]   stress seed at v17: {n} rows')
con.close()

print()
print(f'RESULT: {"ALL SQL VALIDATES" if fails == 0 else f"{fails} FAILURE(S)"}')
sys.exit(1 if fails else 0)
