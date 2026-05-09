-- Drop unused fields from language_camp_projects (kept in code/docs no longer)
ALTER TABLE language_camp_projects
    DROP COLUMN IF EXISTS max_people,
    DROP COLUMN IF EXISTS grades,
    DROP COLUMN IF EXISTS lesson_groups;

