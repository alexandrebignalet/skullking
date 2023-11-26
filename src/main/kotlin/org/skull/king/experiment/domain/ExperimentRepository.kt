package org.skull.king.experiment.domain

import jakarta.inject.Singleton

@Singleton
class ExperimentRepository {

    private val entities = mutableMapOf<String, Experiment>()

    fun save(experiment: Experiment) {
        entities[experiment.id] = experiment
    }

    operator fun get(id: String): Experiment? {
        return entities[id]
    }

    fun findAll(): List<Experiment> {
        return entities.values.toList()
    }
}
