package com.genixo.ges.languagecamp;

import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampProject;

public final class LanguageCampApplicationFeeSupport {

    private LanguageCampApplicationFeeSupport() {}

    public static void applyFromProject(LanguageCampApplication application, LanguageCampProject project) {
        application.setPriceAmount(project.getPrice());
        application.setPriceCurrency(project.getCurrency());
    }
}
