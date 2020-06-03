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
import com.google.gson.Gson;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.Comment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/contact-me")
public class DataServlet extends HttpServlet {

    private ArrayList<Comment> comments;

    @Override
    public void init() {
        comments = new ArrayList<>();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");
        String json = new Gson().toJson(comments);
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the input from the form.
        Comment c = getComment(request);

        comments.add(c);

        // Make sure it went through
        System.out.println(comments.get(0).getName());
        System.out.println(comments.get(0).getEmail());
        System.out.println(comments.get(0).getSubject());
        System.out.println(comments.get(0).getMessage());

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", comments.get(0).getName());
        commentEntity.setProperty("email", comments.get(0).getEmail());
        commentEntity.setProperty("subject", comments.get(0).getSubject());
        commentEntity.setProperty("message", comments.get(0).getMessage());

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html");
    }

    private Comment getComment(HttpServletRequest request) {
        // Get the input from the form.
        String name = request.getParameter("name-input");
        String email = request.getParameter("email-input");
        String subject = request.getParameter("subject-input");
        String message = request.getParameter("message-input");

        // Construct comment object.
        Comment c = new Comment(name, email, subject, message);      

        return c;
    }
}
