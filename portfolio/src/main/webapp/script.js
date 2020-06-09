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

var map;
var locationsDict = new Object();

/** Calls all necessary functions to be called onLoad */
function initialize() {
    loadComments();
    createMap();
}

/** Creates a map with all components and adds it to the page. */
function createMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 41.8996099, lng: -87.6232902, h: -7, p: 22},
    zoom: 13
  });

  setupMarkers();
  changeView(locationsDict["Bean"]);
}

/** All setup code for markers. */
function setupMarkers() {
  locationsDict["Bean"] = {
    m: null, 
    lat: 41.8826099, 
    lng: -87.6232902, 
    h: -7, 
    p: 22,
    is: '<p style="margin-top:-3px; margin-bottom:-3px" class="field-text">Cloud Gate</p>',
    iw: null
  };
  locationsDict["Skyline"] = {
    m: null, 
    lat: 41.9147471, 
    lng: -87.6209924, 
    h:185, 
    p:17,
    is: '<p style="margin-top:-3px; margin-bottom:-3px" class="field-text">Photography Point</p>',
    iw: null
  };
  locationsDict["Riverwalk"] = {
    m: null, 
    lat: 41.8877563, 
    lng: -87.6273952, 
    h:125, 
    p:30,
    is: '<p style="margin-top:-3px; margin-bottom:-3px" class="field-text">Chicago Riverwalk</p>',
    iw: null
  };

  for (var location in locationsDict) {
    locationsDict[location].m = createMarker(locationsDict[location]);
    locationsDict[location].iw = new google.maps.InfoWindow({content: locationsDict[location].is});
    addMarkerListeners(locationsDict[location].m, location, locationsDict[location].iw); 
  } 
}

/** Add bounce animation and view listeners. */
function addMarkerListeners(marker, place, infowindow) {
    google.maps.event.addListener(marker, 'click', function() {
      changeView(locationsDict[place]);
    }); 
    google.maps.event.addListener(marker, 'mouseover', function() {
      marker.setAnimation(google.maps.Animation.BOUNCE);
    }); 
    google.maps.event.addListener(marker, 'mouseout', function() {
      marker.setAnimation(null);
    });
    marker.addListener('mouseover', function() {
      infowindow.open(map, marker);
    });
    marker.addListener('mouseout', function() {
      infowindow.close(map, marker);
    });
}

/** Change street view to position in positionTuple. */
function changeView(positionTuple) {
    var panorama = new google.maps.StreetViewPanorama(
      document.getElementById('pano'), {
        position: {lat: positionTuple.lat, lng: positionTuple.lng},
        pov: {
          heading: positionTuple.h,
          pitch: positionTuple.p
        }
      });
  map.setStreetView(panorama);
}

/** Initialize and set up individual markers. */
function createMarker(positionTuple) {
  console.log("Created marker");
  var tmp = new google.maps.Marker({
    position: {lat: positionTuple.lat, lng: positionTuple.lng},
    animation: google.maps.Animation.DROP,
    map: map,
    icon: {
        url: "http://maps.google.com/mapfiles/ms/icons/pink-dot.png",
        scaledSize: new google.maps.Size(45, 45) //pixels
    },
  });
  return tmp;
}

/** Fetches comments from the server and adds them to the DOM. */
async function loadComments() {
  const value = document.getElementById("num-comments").value;
  const response = await fetch(`/list-comments?num-comments=${value}`);
  const comments = await response.json();
  const commentsDisplayed = document.getElementById("comment-list");
  commentsDisplayed.innerHTML = '';

  comments.forEach((comment) => {
    commentsDisplayed.appendChild(createCommentElement(comment));
  });
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement("li");
  commentElement.className = "comment";

  const titleElement = document.createElement("span");
  titleElement.innerText = comment.message;

  const deleteButtonElement = document.createElement("button");
  deleteButtonElement.innerText = "Delete";
  deleteButtonElement.addEventListener("click", () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(titleElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append("id", comment.id);
  fetch("/delete-comment", { method: "POST", body: params });
}

function deleteAllComments() {
  fetch("delete-all-comments", { method: "POST" });
  document.getElementById("comment-list").remove();
}
