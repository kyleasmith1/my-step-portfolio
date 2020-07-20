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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {

  /** 
    * Creates a query that allows for different possible meeting time ranges to be found 
    * given any request for a meeting throughout a day
    */

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> timeCollection = new ArrayList<TimeRange>();
    ArrayList<TimeRange> relevantTimes = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalTimes = new ArrayList<TimeRange>();
    int temporaryStart = 0;

    // Base Case
    if (request.getDuration() > 1440) {
        return Arrays.asList();
    }

    // Separates events into mandatory or optional time ranges
    for (Event event: events) {
        if (!Collections.disjoint(request.getAttendees(), event.getAttendees())
            || (!Collections.disjoint(request.getOptionalAttendees(), event.getAttendees())
                && request.getAttendees().size() == 0)) {
            relevantTimes.add(event.getWhen());
        }
        if (!Collections.disjoint(request.getOptionalAttendees(), event.getAttendees())) {
            optionalTimes.add(event.getWhen());
        }
    }

    if (relevantTimes.size() == 0 && optionalTimes.size() == 0) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Find possible ranges by looking at mandatory meeting times
    for(TimeRange time: relevantTimes) {
        if (time.contains(temporaryStart)) {
            temporaryStart = time.end();
        }
        if (time.start() > temporaryStart) {
            if (request.getDuration() <= (time.start() - temporaryStart)) {
                timeCollection.add(TimeRange.fromStartEnd(temporaryStart, time.start(), false));
            }
            temporaryStart = time.end();
        }    
    }

    TimeRange removeRange = null;
    TimeRange leftSplit = null;
    TimeRange rightSplit = null;

    // Goes through optional events and checks if meetings should be scheduled during those times or not
    for (TimeRange time: optionalTimes) {
        for (TimeRange collection: timeCollection) {
            if (request.getDuration() <= time.duration() && collection.contains(time)) {
                removeRange = collection;
                if (!collection.equals(time)) {
                   leftSplit = TimeRange.fromStartEnd(collection.start(), time.start(), false);
                   rightSplit = TimeRange.fromStartEnd(time.end(), collection.end(), false);
                }
            }
        }
    }

    timeCollection.remove(removeRange);
    
    if (leftSplit != null && request.getDuration() <= leftSplit.duration()) {
        timeCollection.add(leftSplit);
    }

    if (rightSplit != null && request.getDuration() <= rightSplit.duration()) {
        timeCollection.add(rightSplit);
    }

    if (temporaryStart != 1440) {
        timeCollection.add(TimeRange.fromStartEnd(temporaryStart, 1440, false));
    }
    return timeCollection;
  } 
}