package uk.gov.ons.validation.entity;

import java.util.Objects;

public class ValidationConfig {

    public String questionCode;
    public String derivedQuestionCode;

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getDerivedQuestionCode() {
        return derivedQuestionCode;
    }

    public void setDerivedQuestionCode(String derivedQuestionCode) {
        this.derivedQuestionCode = derivedQuestionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationConfig)) return false;
        ValidationConfig that = (ValidationConfig) o;
        return Objects.equals(getQuestionCode(), that.getQuestionCode()) &&
                Objects.equals(getDerivedQuestionCode(), that.getDerivedQuestionCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionCode(), getDerivedQuestionCode());
    }



}
