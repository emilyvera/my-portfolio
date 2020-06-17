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

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public final class FindMeetingQuery {

  /** 
   * Return all possible time ranges to hold a meeting of specific duration.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Find which times work for mandatory attendees
    ArrayList<TimeRange> busyRangesForMandatoryAttendees = findBusyRanges(request.getAttendees(), events);
    Collections.sort(busyRangesForMandatoryAttendees, TimeRange.ORDER_BY_START);
    Collection<TimeRange> freeRangesForMandatoryAttendees = findFreeRanges(request, busyRangesForMandatoryAttendees);
    
    // Factor in optional attendees if there are any
    if (!request.getOptionalAttendees().isEmpty()) {
      ArrayList<TimeRange> busyRangesForOptionalAttendees = findBusyRanges(request.getOptionalAttendees(), events);
      Collections.sort(busyRangesForOptionalAttendees, TimeRange.ORDER_BY_START);
      
      ArrayList<TimeRange> busyRangesForAllAttendees = new ArrayList<>();
      busyRangesForAllAttendees.addAll(busyRangesForMandatoryAttendees);
      busyRangesForAllAttendees.addAll(busyRangesForOptionalAttendees);
      Collections.sort(busyRangesForAllAttendees, TimeRange.ORDER_BY_START);
      Collection<TimeRange> freeRangesForAllAttendees = findFreeRanges(request, busyRangesForAllAttendees);
      
      // Handles case where there are no mandatory attendees but optional attendees have no availability
      if (freeRangesForAllAttendees.isEmpty() && request.getAttendees().isEmpty()) {
        return Collections.emptyList();
      }


      // If more than on time slot exists to accommodate both mandatory & optional attendees, return that
      if (!freeRangesForAllAttendees.isEmpty()) {
        return freeRangesForAllAttendees;
      }
    }

    return freeRangesForMandatoryAttendees;
  }

  /** 
   * Returns an ArrayList of all busy TimeRanges.
   * Check all events for the day and add TimeRanges of events with our mandatory attendees.
  */
  private ArrayList<TimeRange> findBusyRanges(Collection<String> attendees, Collection<Event> events) {
    ArrayList<TimeRange> busyRanges = new ArrayList<TimeRange>();
    for (Event event : events){
      for (String attendee : attendees) {
        if (event.getAttendees().contains(attendee)){
          busyRanges.add(event.getWhen());
        }      
      }
    }
    return busyRanges;
  }

  /** 
   * Returns a Collection of all free TimeRanges.
   * Finds free TimeRanges in between busy TimeRanges and takes duration into account.
  */
  private Collection<TimeRange> findFreeRanges(MeetingRequest request, Collection<TimeRange> busyRanges) {
    Collection<TimeRange> freeRanges = new ArrayList<TimeRange>();

    // Default values
    int startFreeRange = TimeRange.START_OF_DAY;
    int endFreeRange = TimeRange.END_OF_DAY; 

    for (TimeRange busyRange : busyRanges) {
      // Free range ends when a busy range starts
      endFreeRange = busyRange.start();

      // Make sure we can fit the meeting in the potential range
      if (request.getDuration() <= endFreeRange - startFreeRange){ 
        freeRanges.add(TimeRange.fromStartEnd(startFreeRange, endFreeRange, false));
      }
      
      // Handles over lapping cases
      startFreeRange = Math.max(startFreeRange, busyRange.end());       
    } 

    // Add the rest of the day, assumming the meeting fits in that time
    if (request.getDuration() < TimeRange.END_OF_DAY - startFreeRange){
      freeRanges.add(TimeRange.fromStartEnd(startFreeRange, TimeRange.END_OF_DAY, true));
    }

    return freeRanges;
  }
}
