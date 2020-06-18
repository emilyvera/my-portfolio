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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class FindMeetingQuery {

  /** 
   * Return all possible time ranges to hold a meeting of specific duration.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Find which times work for mandatory attendees
    ImmutableList<TimeRange> sortedBusyRangesForMandatoryAttendees = findBusyRanges(request.getAttendees(), events);
    ImmutableList<TimeRange> freeRangesForMandatoryAttendees = findFreeRanges(request, sortedBusyRangesForMandatoryAttendees);
    
    // Factor in optional attendees if there are any
    if (!request.getOptionalAttendees().isEmpty()) {
      ImmutableList<TimeRange> freeRangesForAllAttendees = findFreeRangesAllAttendees(sortedBusyRangesForMandatoryAttendees, events, request);

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
   * Returns an ImmutableList of all busy TimeRanges.
   * Check all events for the day and add TimeRanges of events with our mandatory attendees.
  */
  private ImmutableList<TimeRange> findBusyRanges(Collection<String> attendees, Collection<Event> events) {
    ArrayList<TimeRange> busyRanges = new ArrayList<>();
    for (Event event : events) {
        // Checks if we have any overlap of attendees
        if (!Collections.disjoint(attendees, event.getAttendees())) {
            busyRanges.add(event.getWhen());
        }
    }

    Collections.sort(busyRanges, TimeRange.ORDER_BY_START);

    return ImmutableList.<TimeRange>builder() 
              .addAll(busyRanges) 
              .build();
  }

  /** 
   * Returns an ImmutableList of all free TimeRanges.
   * Finds free TimeRanges in between sorted, busy TimeRanges and takes duration into account.
  */
  private ImmutableList<TimeRange> findFreeRanges(MeetingRequest request, ImmutableList<TimeRange> sortedBusyRanges) {
    ArrayList<TimeRange> freeRanges = new ArrayList<>();

    int startFreeRange = TimeRange.START_OF_DAY;

    for (TimeRange sortedBusyRange : sortedBusyRanges) {
      // Free range ends when a busy range starts
      int endFreeRange = sortedBusyRange.start();

      // Make sure we can fit the meeting in the potential range
      if (request.getDuration() <= endFreeRange - startFreeRange){ 
        freeRanges.add(TimeRange.fromStartEnd(startFreeRange, endFreeRange, false));
      }
      
      // Handles over lapping cases
      startFreeRange = Math.max(startFreeRange, sortedBusyRange.end());       
    } 

    // Add the rest of the day, assumming the meeting fits in that time
    if (request.getDuration() < TimeRange.END_OF_DAY - startFreeRange){
      freeRanges.add(TimeRange.fromStartEnd(startFreeRange, TimeRange.END_OF_DAY, true));
    }

    return ImmutableList.<TimeRange>builder() 
              .addAll(freeRanges) 
              .build();
  }

  /** 
   * Returns an ImmutableList of all free TimeRanges.
   * Finds free TimeRanges for both optional and mandatory attendess by considering spaces in between sorted, 
   * busy TimeRanges and takes duration into account.
  */
  private ImmutableList<TimeRange> findFreeRangesAllAttendees(ImmutableList<TimeRange> sortedBusyRangesForMandatoryAttendees, Collection<Event> events, MeetingRequest request) {
    ImmutableList<TimeRange> sortedBusyRangesForOptionalAttendees = findBusyRanges(request.getOptionalAttendees(), events);
      
    ArrayList<TimeRange> busyRangesForAllAttendees = new ArrayList<>();
    busyRangesForAllAttendees.addAll(sortedBusyRangesForMandatoryAttendees);
    busyRangesForAllAttendees.addAll(sortedBusyRangesForOptionalAttendees);
    Collections.sort(busyRangesForAllAttendees, TimeRange.ORDER_BY_START);
    ImmutableList<TimeRange> immutableBusyRangesForAllAttendees = ImmutableList.<TimeRange>builder() 
              .addAll(busyRangesForAllAttendees) 
              .build();
    ImmutableList<TimeRange> freeRangesForAllAttendees = findFreeRanges(request, immutableBusyRangesForAllAttendees);

    return freeRangesForAllAttendees;
  }
}
