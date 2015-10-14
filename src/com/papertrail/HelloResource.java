package com.papertrail;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;


@Path("/analysis")
public class HelloResource {

	@GET
	public String analysis(
			@FormParam("title") String title,
			@FormParam("keywords") String keywords,
			@FormParam("summary") String summary) {		
		return "test topic," + keywords;
	}
}