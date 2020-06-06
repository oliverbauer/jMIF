/**
 * 
 */
package io.github.jmif;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;

/**
 * @author thebrunner
 *
 */
@ApplicationPath("/jmif")
public class RESTService extends Application {

	private final ServiceDelegate delegate = new ServiceDelegate();
	
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("id") long id) throws MIFException {
		return Response.ok(delegate.get(id)).build();
	}
	
	@POST
	@Path("/exportImage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response exportImage(MIFProject pr, String output, int frame) throws MIFException {
		return Response.accepted(delegate.exportImage(pr, output, frame)).build();
	}

	@POST
	@Path("/convert")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response convert(MIFProject pr, boolean preview) throws MIFException {
		return Response.accepted(delegate.convert(pr, preview)).build();
	}

	@POST
	@Path("/updateFramerate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateFramerate(MIFProject project) {
		return Response.accepted(delegate.updateFramerate(project)).build();
	}

	@POST
	@Path("/createWorkingDirs")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createWorkingDirs(MIFProject project) {
		return Response.accepted(delegate.createWorkingDirs(project)).build();
	}

	@POST
	@Path("/createVideo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createVideo(String file, String display, float frames, String dim, int overlay, String workingDir, int profileFramelength) throws MIFException {
		return Response.accepted(delegate.createVideo(file, display, frames, dim, overlay, workingDir, profileFramelength)).build();
	}

	@POST
	@Path("/createPreview")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPreview(MIFFile file, String workingDir) throws MIFException {
		return Response.accepted(delegate.createPreview(file, workingDir)).build();
	}

	@POST
	@Path("/createManualPreview")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createManualPreview(MIFImage image) {
		return Response.accepted(delegate.createManualPreview(image)).build();
	}

	@POST
	@Path("/createImage")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createImage(String file, String display, float frames, String dim, int overlay, String workingDir, int framelength) throws MIFException {
		return Response.accepted(delegate.createImage(file, display, frames, dim, overlay, workingDir, framelength)).build();
	}

	@POST
	@Path("/createAudio")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createAudio(String path) throws MIFException {
		return Response.accepted(delegate.createAudio(path)).build();
	}

	@POST
	@Path("/getProfiles")
	public Response getProfiles() throws MIFException {
		return Response.accepted(delegate.getProfiles()).build();
	}

	@POST
	@Path("/getFilters")
	public Response getFilters() throws MIFException {
		return Response.accepted(delegate.getFilters()).build();
	}
	
	@POST
	@Path("/getFilterDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getFilterDetails(String filter) throws MIFException {
		return Response.accepted(delegate.getFilterDetails(filter)).build();
	}

}
