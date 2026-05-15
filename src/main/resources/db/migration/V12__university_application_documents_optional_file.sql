-- Allow document rows without a file URL (checklist first, upload later via update)

ALTER TABLE university_application_documents
    ALTER COLUMN document_url DROP NOT NULL,
    ALTER COLUMN uploaded_at DROP NOT NULL;
