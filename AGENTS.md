# Repository agent instructions

## Command skills

Treat user input whose first token has the form `./<command>` as a repository skill invocation.

1. Remove the leading `./` to obtain `<command>`.
2. Look for `.ai/skills/<command>/SKILL.md` relative to the repository root.
3. If the file exists, read it completely before taking task actions and follow it for that turn. Treat text after the first token as arguments or context for the skill.
4. Tell the user which repository skill is being used and why.
5. If no matching skill exists, state that `.ai/skills/<command>/SKILL.md` was not found and ask the user how to proceed. Do not guess a different command or execute the token as a shell command.

For `./create-domain-entity`, use `.ai/skills/create-domain-entity/SKILL.md`.

## Testing scope

Unit testing and mutation testing must exclusively target production code under `api/src/main/java/com/gustavo/dev/api/domains`.
