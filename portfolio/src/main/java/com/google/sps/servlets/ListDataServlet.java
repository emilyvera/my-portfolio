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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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

  // Only comments with sentiment score >= this value will be fetched and displayed.
  private static double COMMENT_FILTER_THRESHOLD = -0.5; 

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numCommentsToDisplay = getNumberOfComments(request);

    Filter niceComments = new FilterPredicate("sentimentScore", FilterOperator.GREATER_THAN_OR_EQUAL, COMMENT_FILTER_THRESHOLD);
    Query query = new Query("Comment").addSort("sentimentScore", SortDirection.DESCENDING);    
    query.setFilter(niceComments);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numCommentsToDisplay));

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      long id = entity.getKey().getId(); 
      String name = (String) entity.getProperty("name");
      String email = (String) entity.getProperty("email");
      String subject = (String) entity.getProperty("subject");
      String message = (String) entity.getProperty("message");
      long timestamp = (long) entity.getProperty("timestamp");
      double sentimentScore = (double) entity.getProperty("sentimentScore");

      Comment comment = new Comment(id, name, email, subject, message, timestamp, sentimentScore);
      comments.add(comment);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  int getNumberOfComments(HttpServletRequest request) {
    String numCommentsString = request.getParameter("num-comments");

    // Convert the input to an int.
    int numComments;
    try {
      numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsString);
      numComments = 0; //default val
    }
    return numComments;
  }
}
