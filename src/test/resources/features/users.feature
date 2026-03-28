Feature: Users API tests

  Background:
    * url baseUrl

  Scenario: Get all users
    Given path '/users'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Get a single user
    Given path '/users/1'
    When method GET
    Then status 200
    And match response.id == 1
    And match response.name == '#string'
    And match response.email == '#string'

  Scenario: Create a new user
    Given path '/users'
    And request { name: 'Test User', email: 'test@example.com' }
    When method POST
    Then status 201
    And match response.name == 'Test User'