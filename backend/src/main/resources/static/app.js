const API = "/api";

const startLocation = document.getElementById("startLocation");
const algorithm = document.getElementById("algorithm");
const locationsContainer = document.getElementById("locations");
const calculateBtn = document.getElementById("calculateBtn");
const errorBox = document.getElementById("error");

const destinationContainer =
    document.getElementById("destination-container");

const destinationLocation =
    document.getElementById("destinationLocation");

let locations = [];


/* =========================
   LOAD LOCATIONS
========================= */

async function loadLocations() {

    try {

        console.log("Fetching locations...");

        const response = await fetch(`${API}/locations`);

        console.log("Response status:", response.status);

        if (!response.ok) {
            throw new Error(
                `Server returned ${response.status}`
            );
        }

        locations = await response.json();

        console.log("Locations received:", locations);

        if (!Array.isArray(locations)) {
            throw new Error("Invalid locations response");
        }

        if (locations.length === 0) {
            throw new Error("No locations found in database");
        }

        populateStartLocation();
        populateLocationCheckboxes();
        populateDestinations();

    } catch (error) {

        console.error("Location loading error:", error);

        showError(
            "Could not load locations: " + error.message
        );

        startLocation.innerHTML =
            `<option value="">Failed to load locations</option>`;

        locationsContainer.innerHTML =
            `<p style="color:red;">
                Failed to load locations.
            </p>`;
    }
}


/* =========================
   START LOCATION DROPDOWN
========================= */

function populateStartLocation() {

    startLocation.innerHTML = "";

    locations.forEach(location => {

        const option = document.createElement("option");

        option.value = location.id;
        option.textContent = location.name;

        startLocation.appendChild(option);

    });

}


/* =========================
   DESTINATION DROPDOWN
========================= */

function populateDestinations() {

    destinationLocation.innerHTML =
        '<option value="">Select destination</option>';

    locations.forEach(location => {

        // Starting location cannot be destination
        if (
            String(location.id) !==
            String(startLocation.value)
        ) {

            const option = document.createElement("option");

            option.value = location.id;
            option.textContent = location.name;

            destinationLocation.appendChild(option);
        }

    });
}


/* =========================
   LOCATION CHECKBOXES
========================= */

function populateLocationCheckboxes() {

    locationsContainer.innerHTML = "";

    locations.forEach(location => {

        const label = document.createElement("label");

        label.className = "location";

        const checkbox =
            document.createElement("input");

        checkbox.type = "checkbox";
        checkbox.value = location.id;
        checkbox.checked = true;
        checkbox.className = "location-checkbox";

        const text =
            document.createElement("span");

        text.textContent = location.name;

        label.appendChild(checkbox);
        label.appendChild(text);

        locationsContainer.appendChild(label);

    });

}


/* =========================
   ALGORITHM CHANGE
========================= */

algorithm.addEventListener("change", function () {

    if (this.value === "dijkstra") {

        // Show destination dropdown
        destinationContainer.style.display = "block";

        // Populate destination options
        populateDestinations();

    } else {

        // Hide destination dropdown
        destinationContainer.style.display = "none";

        destinationLocation.value = "";
    }

});


/* =========================
   START LOCATION CHANGE
========================= */

startLocation.addEventListener("change", function () {

    const startId = Number(this.value);

    // Keep starting location selected
    document
        .querySelectorAll(".location-checkbox")
        .forEach(checkbox => {

            if (Number(checkbox.value) === startId) {
                checkbox.checked = true;
            }

        });

    // Refresh Dijkstra destinations
    populateDestinations();

});


/* =========================
   CALCULATE ROUTE
========================= */

calculateBtn.addEventListener(
    "click",
    calculateRoute
);


async function calculateRoute() {

    hideError();

    const startId = startLocation.value;


    /*
     * Validate starting location
     */

    if (!startId) {

        showError(
            "Please select a starting location."
        );

        return;
    }


    /*
     * Dijkstra validation
     */

    let destinationId = null;

    if (algorithm.value === "dijkstra") {

        destinationId =
            destinationLocation.value;

        if (!destinationId) {

            showError(
                "Please select a destination."
            );

            return;
        }

        if (Number(destinationId) === Number(startId)) {

            showError(
                "Starting location and destination must be different."
            );

            return;
        }
    }


    /*
     * Get selected delivery locations
     */

    const selectedCheckboxes =
        document.querySelectorAll(
            ".location-checkbox:checked"
        );


    let selectedIds =
        Array.from(selectedCheckboxes)
            .map(checkbox =>
                Number(checkbox.value)
            );


    /*
     * Make sure starting location
     * is included for TSP algorithms
     */

    const startIdNumber = Number(startId);

    if (!selectedIds.includes(startIdNumber)) {

        selectedIds.push(startIdNumber);

    }


    /*
     * Validate locations
     *
     * Greedy / Branch & Bound:
     * Need at least two locations.
     *
     * Dijkstra:
     * Only needs start + destination.
     */

    if (
        algorithm.value !== "dijkstra" &&
        selectedIds.length < 2
    ) {

        showError(
            "Please select at least two locations."
        );

        return;
    }


    /*
     * Disable button while calculating
     */

    calculateBtn.disabled = true;
    calculateBtn.textContent = "Calculating...";


    try {

        console.log(
            "=============================="
        );

        console.log(
            "Calculating route..."
        );

        console.log(
            "Algorithm:",
            algorithm.value
        );

        console.log(
            "Start ID:",
            startIdNumber
        );

        console.log(
            "Destination ID:",
            destinationId
        );

        console.log(
            "Location IDs:",
            selectedIds
        );


        /*
         * Send request to Spring Boot
         */

        const response = await fetch(
            `${API}/routes/${algorithm.value}`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    startId: startIdNumber,

                    destinationId:
                        destinationId
                            ? Number(destinationId)
                            : null,

                    locationIds: selectedIds

                })
            }
        );


        console.log(
            "Route response status:",
            response.status
        );


        /*
         * Handle backend error
         */

        if (!response.ok) {

            const message =
                await response.text();

            console.error(
                "Backend error:",
                message
            );

            throw new Error(
                message ||
                `Server returned ${response.status}`
            );
        }


        /*
         * Convert response to JSON
         */

        const result =
            await response.json();


        console.log(
            "Route result:",
            result
        );


        /*
         * Display result
         */

        displayResult(result);


    } catch (error) {

        console.error(
            "Route calculation error:",
            error
        );

        showError(
            "Route calculation failed: " +
            error.message
        );


    } finally {

        calculateBtn.disabled = false;

        calculateBtn.textContent =
            "Calculate Route";

    }

}


/* =========================
   DISPLAY RESULT
========================= */

function displayResult(result) {

    const resultCard =
        document.getElementById(
            "resultCard"
        );

    resultCard.style.display = "block";


    /*
     * Algorithm
     */

    document.getElementById(
        "resultAlgorithm"
    ).textContent =
        result.algorithm || "-";


    /*
     * Total distance
     */

    document.getElementById(
        "resultDistance"
    ).textContent =
        `${result.totalDistance ?? 0} km`;


    /*
     * Execution time
     *
     * Backend returns nanoseconds.
     * Convert nanoseconds → microseconds.
     */

    const executionTimeNanos =
        result.executionTimeNanos ?? 0;

    const executionTimeMicros =
        executionTimeNanos / 1000;


    document.getElementById(
        "resultTime"
    ).textContent =
        `${executionTimeMicros.toFixed(2)} μs`;


    /*
     * Nodes explored
     */

    document.getElementById(
        "resultNodes"
    ).textContent =
        result.nodesExplored ?? 0;


    /*
     * Route
     */

    const routeContainer =
        document.getElementById(
            "resultRoute"
        );


    routeContainer.innerHTML = "";


    if (
        result.routeNames &&
        result.routeNames.length > 0
    ) {

        result.routeNames.forEach(
            (name, index) => {

                /*
                 * Location name
                 */

                const node =
                    document.createElement(
                        "span"
                    );

                node.className =
                    "route-node";

                node.textContent =
                    name;

                routeContainer.appendChild(
                    node
                );


                /*
                 * Arrow
                 */

                if (
                    index <
                    result.routeNames.length - 1
                ) {

                    const arrow =
                        document.createElement(
                            "span"
                        );

                    arrow.className =
                        "arrow";

                    arrow.textContent =
                        " → ";

                    routeContainer.appendChild(
                        arrow
                    );

                }

            }
        );

    } else {

        routeContainer.textContent =
            "No route returned.";

    }

}


/* =========================
   ERROR HANDLING
========================= */

function showError(message) {

    errorBox.textContent = message;

    errorBox.style.display = "block";

}


function hideError() {

    errorBox.textContent = "";

    errorBox.style.display = "none";

}


/* =========================
   START APPLICATION
========================= */

loadLocations();