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


/** Fetches comments from the server and adds them to the DOM. */
async function loadComments() {
  const value = document.getElementById("num-comments").value;
  const response = await fetch(`/list-comments?num-comments=${value}`);
  const comments = await response.json();
  const commentsDisplayed = document.getElementById("comment-list");
  commentsDisplayed.innerHTML = '';

  comments.forEach((comment) => {
    commentsDisplayed.appendChild(createCommentElement(comment));
  });
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement("li");
  commentElement.className = "comment";

  const titleElement = document.createElement("span");
  titleElement.innerText = comment.message;

  const deleteButtonElement = document.createElement("button");
  deleteButtonElement.innerText = "Delete";
  deleteButtonElement.addEventListener("click", () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(titleElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append("id", comment.id);
  fetch("/delete-comment", { method: "POST", body: params });
}

function deleteAllComments() {
  fetch("delete-all-comments", { method: "POST" });
  document.getElementById("comment-list").remove();
}
