// Allure 3 report settings: `npx allure generate target/allure-results` turns the results from `mvn test`
// into target/allure-report/index.html (plain object, so no npm install is needed in this Java project).
export default {
  name: "Swedbank FX API · test report",
  output: "./target/allure-report",
  plugins: {
    awesome: {
      options: {
        reportName: "Swedbank FX API · test report",
        reportLanguage: "en",
        singleFile: true, // one self-contained index.html: opens straight from a downloaded CI artifact
      },
    },
  },
};
