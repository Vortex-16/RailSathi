process.env.PORT = '8095';
import '../src/server';
import { runAllValidationTests } from './production_validation';
import { runStage3Validation } from './stage3_validation';

// Wait 1 second for the HTTP server to bind to test port
setTimeout(async () => {
  try {
    await runAllValidationTests();
    await runStage3Validation();
    process.exit(0);
  } catch (err) {
    console.error('Test execution failed with error:', err);
    process.exit(1);
  }
}, 1000);
