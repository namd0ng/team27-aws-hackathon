# Unresolved Reference Prevention Rule

## CRITICAL: Unresolved Reference Prevention
- **MUST**: Always verify that all referenced functions, classes, and variables are properly defined and imported before using them
- **MUST**: Check that all enum classes, data classes, and composable functions are declared at the correct scope level
- **MUST**: Ensure all imports are present and correct
- **NEVER**: Use functions, classes, or variables that don't exist or are out of scope
- **NEVER**: Place private modifiers on local functions inside composables
- **NEVER**: Declare enum classes inside composable functions