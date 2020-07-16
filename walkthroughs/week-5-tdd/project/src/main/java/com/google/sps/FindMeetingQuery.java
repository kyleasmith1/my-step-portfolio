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
  
  /** Creates a query that allows for different possible meeting time ranges to be found 
    * given any request for a meeting throughout a day
    */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Base Case(s)
    if (request.getDuration() > 1440) {
        return Arrays.asList(); 
    }

    // General Case(s)
    int startAt = 0;
    int endAt = 0;
    int similarities = 0;

    ArrayList<TimeRange> queryCollection = new ArrayList<TimeRange>();

    for(Event event: events) {
        int eventStart = event.getWhen().start();
        int eventEnd = event.getWhen().end();

        // Checks to see if there is at least one similar attendee during an event
        if (!Collections.disjoint(request.getAttendees(), event.getAttendees())) {
            similarities += 1;

            if (eventStart <= startAt && eventEnd > startAt) {
                startAt = eventEnd;
            }

            endAt = eventStart;
            if (endAt > startAt) {
                if (request.getDuration() <= (endAt - startAt)) {
                    queryCollection.add(TimeRange.fromStartEnd(startAt, endAt, false));
                }

                startAt = eventEnd;
            }    
        }
    }

    if (similarities == 0) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    if (startAt != 1440) {
        queryCollection.add(TimeRange.fromStartEnd(startAt, 1440, false));
    }

    return queryCollection;
  } 
}