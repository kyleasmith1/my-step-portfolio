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

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      
    // Base Case(s)
    if (request.getDuration() > 1440) {
        return Arrays.asList(); 
    }

    // General Case(s)
    int startTemp = 0;
    int endTemp = 0;
    int similarities = 0;

    ArrayList<TimeRange> queryCollection = new ArrayList<TimeRange>();

    for(Event event: events) {
        int eventStart = event.getWhen().start();
        int eventEnd = event.getWhen().end();

        // At least one similar attendee
        if (!Collections.disjoint(request.getAttendees(), event.getAttendees())) {
            similarities += 1;

            // Checks for overlap
            if (eventStart <= startTemp && eventEnd > startTemp) {
                startTemp = eventEnd;
            }

            endTemp = eventStart;
            if (endTemp > startTemp) {
                long durationTemp = endTemp - startTemp;

                if (request.getDuration() <= durationTemp) {
                    queryCollection.add(TimeRange.fromStartEnd(startTemp, endTemp, false));
                }

                startTemp = eventEnd;
            }    
        }
    }

    if (similarities == 0) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    if (startTemp != 1440) {
        queryCollection.add(TimeRange.fromStartEnd(startTemp, 1440, false));
    }

    return queryCollection;
  } 
}