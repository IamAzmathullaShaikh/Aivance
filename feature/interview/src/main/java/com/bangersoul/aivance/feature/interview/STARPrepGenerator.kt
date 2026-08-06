package com.bangersoul.aivance.feature.interview

import com.bangersoul.aivance.core.common.model.InterviewQuestion

object STARPrepGenerator {

    /**
     * Generates a role-specific STAR (Situation, Task, Action, Result) interview prep question pack.
     */
    fun generateStarPack(role: String): List<InterviewQuestion> {
        val roleTitle = role.ifBlank { "Software Engineer" }
        return listOf(
            InterviewQuestion(
                id = System.currentTimeMillis() + 1,
                text = "Describe a challenging technical problem you solved as a $roleTitle using the STAR method.",
                category = "BEHAVIORAL",
                difficulty = "MEDIUM",
                expectedKeyPoints = listOf(
                    "Situation: Context & scope of the problem",
                    "Task: Your specific responsibilities & constraints",
                    "Action: Concrete technical steps & decisions taken",
                    "Result: Quantitative business impact & lessons learned"
                ),
                idealAnswer = "Situation: At my previous company, our production microservice experienced severe memory leaks under peak traffic.\nTask: As the lead engineer, I was responsible for diagnosing the root cause and deploying a zero-downtime fix within 48 hours.\nAction: I captured heap dumps using async-profiler, identified unclosed database connection pools, refactored thread resource management, and added Automated Prometheus alerts.\nResult: System latency dropped by 40%, zero crashes occurred during peak sale event, and team adopted automated connection pool linting."
            ),
            InterviewQuestion(
                id = System.currentTimeMillis() + 2,
                text = "Tell me about a time you had a cross-functional disagreement regarding architectural trade-offs.",
                category = "LEADERSHIP",
                difficulty = "HARD",
                expectedKeyPoints = listOf(
                    "Situation: Differing engineering vs product priorities",
                    "Task: Aligning stakeholders without compromising system reliability",
                    "Action: Benchmarking performance metrics & facilitating consensus workshop",
                    "Result: Successful delivery on deadline with 99.9% uptime target met"
                ),
                idealAnswer = "Situation: Product wanted to launch a new analytics feature immediately using quick REST polling, while engineering advocated for WebSocket streaming to reduce backend load.\nTask: Bridge the perspective gap and deliver a scalable solution without delaying launch.\nAction: I built a 1-day benchmark demo comparing server cost & latency under 10k users, presented the data objectively to product leads, and proposed a hybrid polling-fallback model.\nResult: Product agreed on WebSockets for initial release, launch was on-time, and backend infra costs were saved by 65%."
            )
        )
    }
}
