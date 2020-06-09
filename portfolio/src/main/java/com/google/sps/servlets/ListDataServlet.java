// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.servlets.Comment;
import com.google.sps.servlets.DataServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing comments. */
@WebServlet("/list-comments")
public class ListDataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("message", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    String numCommentsString = request.getParameter("num-comments");

    // Convert the input to an int.
    int numComments;
    try {
        numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
        System.err.println("Could not convert to int: " + numCommentsString);
        numComments = 0; //default val
    }

    System.out.println("Num comments to show: " + numComments);

    int numCommentsDisplayed = 0;

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
        if (numCommentsDisplayed < numComments) {
            long id = entity.getKey().getId(); 
            String name = (String) entity.getProperty("name");
            String email = (String) entity.getProperty("email");
            String subject = (String) entity.getProperty("subject");
            String message = (String) entity.getProperty("message");

            Comment c = new Comment(id, name, email, subject, message);
            comments.add(c);
        }
        numCommentsDisplayed++;
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }
}
