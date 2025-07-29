
- 
Apply volatile to Shared State:


	- For: Ensuring memory visibility of high-frequency data (like car positions and traffic light states) between the simulation thread (writer) and the UI thread (reader).
- 
Review for Atomicity with synchronized:


	- For: Protecting compound actions (methods that update multiple related variables at once) to prevent other threads from reading an inconsistent, intermediate state.