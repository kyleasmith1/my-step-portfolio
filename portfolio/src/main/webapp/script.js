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

 function addRandomQuote() {
    const quotes =  [
        'The most common way people give up their power is by thinking they don’t have any.',
        'The most difficult thing is the decision to act, the rest is merely tenacity.',
        'We become what we think about.',
        'Either you run the day, or the day runs you.', 
        'Fall seven times and stand up eight.',
        'You become what you believe.', 'Dream big and dare to fail.',
    ];

    // Assign Random Quote
    const quoteContainer = document.getElementById('quote-container');

    let quote = quotes[Math.floor(Math.random() * quotes.length)];

    while (quoteContainer.innerText === quote) {
        quote = quotes[Math.floor(Math.random() * quotes.length)];
    }

    quoteContainer.innerText = quote;
}

function getServerMessageUsingArrowFunctions(){
  fetch('/data').then(response => response.json()).then((data) => {
      document.getElementById('message-container').innerText = data;
  });
}
