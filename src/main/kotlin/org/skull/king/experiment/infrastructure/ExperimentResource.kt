package org.skull.king.experiment.infrastructure

import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.skull.king.experiment.domain.Experiment
import org.skull.king.experiment.domain.ExperimentRepository


@PermitAll
@Path("/experiments")
class ExperimentResource {
    @Inject
    lateinit var experimentRepository: ExperimentRepository


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): List<Experiment> {
        return experimentRepository.findAll()
    }


    @GET
    @Path("/{experiment_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun addBot(
        @PathParam("experiment_id") experimentId: String
    ): Experiment? {
        return experimentRepository[experimentId]
    }
}
