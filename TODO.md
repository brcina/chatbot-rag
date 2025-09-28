## Project TODOs

### 📝 To Do
- [X] Implementing embedding in the document processor
- [ ] Implement persistence in document processor with postgres vector db and working data jdbc 
- [ ] Optimize the error messages in the processor since they are not using log.error(message, error)
- [ ] Fix the logger output missing logback etc. probably since the project is AI generated
- [ ] Implement also the possibility to rechunk and re embed the already processed files

### ✅ Completed
- [x] Project structure and architecture implemented
- [x] Module organization with separated GPU/CPU instances
- [x] Groovy conversion and Java 21 upgrade
- [x] Build configuration and deployment profiles