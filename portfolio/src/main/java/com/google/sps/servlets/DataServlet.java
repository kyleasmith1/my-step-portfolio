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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.data.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating tasks. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  /** Takes in comment data and creates tasks that contain those comments in the
  language specified by the user */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    PreparedQuery results = datastore.prepare(query);

    // Grabs the desired comment language from the user
    String language = getParameter(request, "languageCode", "en");

    // Adds comment data to the list of tasks
    List<Task> tasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String og_comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

      /* Translates the comments and creates a task element that contains
      the translated comment*/
      Translation trans_comment =
        translate.translate(og_comment, Translate.TranslateOption.targetLanguage(language));

      String comment = trans_comment.getTranslatedText();
      
      Task task = new Task(id, comment, timestamp);
      tasks.add(task);
    }

    /** Grabs the desired number of comments to be displayed on the page from the user 
    and sends that number of comments back*/
    int bounded_task = getUnsignedIntParameter(request, response, "comment-bound", 0);

    List tasks_ = tasks.subList(0, Math.min(tasks.size(), bounded_task));

    response.setContentType("application/json");
    String json = new Gson().toJson(tasks_);
    response.getWriter().println(json);
  }

  /** Creates taskEntity objects that contain comment data and the times that individual
  comments were/are posted and stores them using DatastoreService */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text = getParameter(request, "text-input", "");
    String username = getParameter(request, "name-input", "Anonymous");
    long timestamp = System.currentTimeMillis();

    String comment = username + ": " + text;

    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("comment", comment);
    taskEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    response.sendRedirect("/index.html");
  }

  // Helper Functions

  /** Requests a string from a user to output as a value, and if no value is input, 
  outputs a passed in default value*/
  private String getParameter(HttpServletRequest request, String comment, String defaultValue) {
    assert defaultValue != null;

    String value = request.getParameter(comment);
    if (value == null || "".equals(value)) {
      return defaultValue;
    }
    return value;
  }

  /** Requests an unsigned integer from a user to output as a value, and if no value 
  is input or the value input is negative outputs a passed in default value */
  private int getUnsignedIntParameter(HttpServletRequest request, HttpServletResponse response, String parameter_name, int defaultValue) throws IOException {
    assert defaultValue > 0;

    try {
        int value = Integer.parseInt(request.getParameter(parameter_name));
        if (value > 0) {
            return value;
        }
        return defaultValue;
    }
    catch (NumberFormatException e) {
        response.sendError(400, "paramater-name must be a valid integer");
        return defaultValue;
    }
  }
}