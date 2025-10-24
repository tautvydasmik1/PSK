# Software Product Quality Measurements and References

## 1. Data Quality (Book Model)
- **Measurement:** All Book fields must be non-null and publication year must be between 1450 and 2100.
- **Unit:** 100% of Book objects in tests must pass these checks.
- **Reference:** Internal data model requirements.

## 2. API Integration & Access Control
- **Measurement:** API returns correct error for invalid login.
- **Unit:** 100% of invalid login attempts must fail with error.
- **Reference:** REST API security best practices.

## 3. Requirements Engineering
- **Measurement:** All requirements must pass DoR and formulation quality checklists.
- **Unit:** 100% of requirements checked against checklist.
- **Reference:** IEEE 29148-2018, see requirements_checklist.md.

## 4. Code Coverage
- **Measurement:** Code coverage percentage (e.g., 80%+ for main classes).
- **Unit:** % of code covered by tests (e.g., 95% Book class coverage).
- **Reference:** JaCoCo (https://www.jacoco.org/jacoco/)

## 5. Test Case Quality
- **Measurement:** All branches and conditions in tests are covered.
- **Unit:** 100% branch coverage in critical classes.
- **Reference:** ISO/IEC 25010:2011

## 6. CI/CD Pipeline
- **Measurement:** All commits must pass build and test stages before merge.
- **Unit:** 100% successful pipeline runs for merged code.
- **Reference:** Project CI/CD policy, e.g., GitHub Actions, Jenkins.

---

**All measurements use discrete units (e.g., 95% code coverage, 100% checklist completion).**
**References and standards are provided for each measurement.**

