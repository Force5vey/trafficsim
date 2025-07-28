- 
Implement Event Bus for Commands:


	- For: Serializing all state-modifying actions (add/delete road, car, etc.) onto the simulation thread to prevent concurrent write access and data races.
- 
Apply volatile to Shared State:


	- For: Ensuring memory visibility of high-frequency data (like car positions and traffic light states) between the simulation thread (writer) and the UI thread (reader).
- 
Implement Graceful Engine Shutdown:


	- For: Preventing the application from hanging on exit by properly terminating the simulation's ExecutorService during the JavaFX application lifecycle stop() method.
- 
Review for Atomicity with synchronized:


	- For: Protecting compound actions (methods that update multiple related variables at once) to prevent other threads from reading an inconsistent, intermediate state.