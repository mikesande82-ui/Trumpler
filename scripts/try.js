// Try the prompt from the command line, without deploying anything:
//
//   ANTHROPIC_API_KEY=sk-ant-... node scripts/try.js "Running late, be there in 10" full
//
// The level is light, medium (the default) or full. RAMBLE_MODEL and
// RAMBLE_EFFORT work here as they do on the server.

import { anthropicRamble, cleanMessage, promptOf, tidy, LEVELS, DEFAULT_LEVEL } from '../api/ramble.js';

const [text, level = DEFAULT_LEVEL] = process.argv.slice(2);
if (!text || !LEVELS[level]) {
  console.error('usage: node scripts/try.js "your message" [light|medium|full]');
  process.exit(1);
}
if (!process.env.ANTHROPIC_API_KEY) {
  console.error('Set ANTHROPIC_API_KEY first.');
  process.exit(1);
}

const t0 = Date.now();
const out = await anthropicRamble(promptOf(cleanMessage(text), level));
console.log(tidy(out.text) || `(no ramble: stop_reason ${out.stop_reason})`);
console.error(`\n${out.model}, ${out.usage.input_tokens} in / ${out.usage.output_tokens} out, ${((Date.now() - t0) / 1000).toFixed(1)} s`);
