package dustin.jpa;

/**
 * Enum representing gender.
 */
public enum Gender
{
   FEMALE { public String getMixedCaseForm(){return "Female";} },
   MALE   { public String getMixedCaseForm(){return "Male";} };
   
   public abstract String getMixedCaseForm();
}
