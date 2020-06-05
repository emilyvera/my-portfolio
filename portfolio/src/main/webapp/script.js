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

// /**
//  * Adds a random greeting to the page.
//  */
// function addRandomGreeting() {
//   const greetings =
//       ['I am 20 years old!', 'My favorite color is pink!',
//       'I am in Phi Sigma Sigma!', 'I used to work at John Deere!',
//       'I am outdoorsy!', 'My favorite race is the 5k!',
//       'I have done other Google programs before!'];

//   // Pick a random greeting.
//   const greeting = greetings[Math.floor(Math.random() * greetings.length)];

//   // Add it to the page.
//   const greetingContainer = document.getElementById('greeting-container');
//   greetingContainer.innerText = greeting;
// }

// async function getText() {
//   const response = await fetch('/data');
//   const txt = await response.text();
//   document.getElementById('txt-container').innerText = txt;
// }

// function getMessages() {
//         console.log("Made it here 1");
//   fetch('/data').then(response => response.json()).then((messages) => {
//     // messages is an object, not a string, so we have to
//     // reference its fields to create HTML content

//     console.log("Made it here 2");

//     const messagesElement = document.getElementById('messages-container');
//     messagesElement.innerHTML = '';
//     messagesElement.appendChild(
//         createListElement('1: ' + messages[0]));
//     messagesElement.appendChild(
//         createListElement('2: ' + messages[1]));
//     messagesElement.appendChild(
//         createListElement('3: ' + messages[2]));
//   });
// }

// function createListElement(text) {
//   const liElement = document.createElement('li');
//   liElement.innerText = text;
//   return liElement;
// }

/** Fetches comments from the server and adds them to the DOM. */
function loadComments() {
  const value = document.getElementById("num-comments").value;
  fetch(`/list-comments?num-comments=${value}`)
    .then((response) => response.json())
    .then((comments) => {
      const oldComments = document.getElementById("comment-list");
      while (oldComments.hasChildNodes()) {
        oldComments.removeChild(oldComments.firstChild);
      }

      const commentListElement = document.getElementById("comment-list");
      comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
      });
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
