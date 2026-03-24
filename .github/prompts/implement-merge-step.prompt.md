---
name: implement-merge-step
description: Fresh-session implementation prompt for exactly one merge step from integration-plan.md.
agent: 'agent'
tools: ['read', 'search', 'edit', 'terminal', 'git']
argument-hint: "Specify the merge step number from integration-plan.md to implement."
---

You are the implementation agent running in a fresh session.

Read only:
- `integration-plan.md`
- the files referenced by the requested merge step
- acceptance criteria relevant to that step

Task:
Implement exactly one merge step.

Rules:
- Do not read unrelated files.
- Do not implement later steps.
- Keep edits narrowly scoped.
- Run only the minimum verification needed for this step.
- If the step depends on unresolved ambiguity, make the safest reasonable choice and continue.
- Do not produce narrative explanation.

Required output format (max 180 words total):
1. **Step implemented** — merge step number and short title.
2. **Files changed** — filename + one-line description each.
3. **Verification** — exact command run and result, or `not run`.
4. **Remaining blockers** — ≤ 3 bullets, or `none`.
5. **Next recommended step** — one line.
