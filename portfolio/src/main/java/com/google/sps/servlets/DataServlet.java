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
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.common.base.Strings;
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

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the input from the form.
        String name = request.getParameter("name-input");
        String email = request.getParameter("email-input");
        String subject = request.getParameter("subject-input");
        String message = request.getParameter("message-input");
        long timestamp = System.currentTimeMillis();
        double sentimentScore = getSentimentScore(message);

        // Error handling - don't allow empty or null values
        if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(subject) && !Strings.isNullOrEmpty(message)) {
            Entity commentEntity = new Entity("Comment");
            commentEntity.setProperty("name", name);
            commentEntity.setProperty("email", email);
            commentEntity.setProperty("subject", subject);
            commentEntity.setProperty("message", message);
            commentEntity.setProperty("timestamp", timestamp);
            commentEntity.setProperty("sentimentScore", sentimentScore);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(commentEntity);
        }

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html");
    }

    double getSentimentScore(String message) throws IOException {
        Document doc = Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
		LanguageServiceClient languageService = LanguageServiceClient.create();
		Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
		double sentimentScore = (double) sentiment.getScore();
		languageService.close();
        return sentimentScore;
    }
}
