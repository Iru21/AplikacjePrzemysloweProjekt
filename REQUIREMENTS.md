# Wymagania projektu Dating App

## Wymagania

### Model danych, Repository i JdbcTemplate
- Encje z @Entity, @Id, @GeneratedValue, @Column  
  Relacje: @OneToMany / @ManyToOne z @JoinColumn, @ManyToMany z @JoinTable  

- Repository rozszerzające JpaRepository<T, ID>  
  Custom query methods (findBy…, @Query), Pageable (Page<T>)  
  Konfiguracja datasource/JPA w application.yml  
  Inicjalizacja bazy przez pliki .sql  

- JdbcTemplate jako dependency  
  SELECT (query + RowMapper), INSERT/UPDATE/DELETE (update)  
  Użycie w serwisie lub DAO  

---

### REST API
- @RestController + @RequestMapping("/api/v1/...")  
  GET (lista z paginacją, single), POST, PUT, DELETE  
  @PathVariable, @RequestBody, @RequestParam  
  ResponseEntity + HTTP 200, 201, 204, 400, 404  

- Springdoc OpenAPI + Swagger UI (/swagger-ui.html)  

---

### Warstwa aplikacji – Business Logic
- @Service, @Transactional  
  Constructor injection  
  Mapowanie Entity ↔ DTO  
  Własne wyjątki (ResourceNotFoundException)  
  @RestControllerAdvice + @ExceptionHandler  

- Walidacja DTO: @NotNull, @NotBlank, @Size, @Email, @Valid  
  Bean Validation (controller + service)  
  Spójne komunikaty błędów  

- @Controller + Model + @ModelAttribute  
  Widoki Thymeleaf (th:each, th:object, th:field)  
  Obsługa błędów (th:errors)  
  Layout (th:fragment, th:replace)  
  Bootstrap 5  

- Upload plików (MultipartFile, multipart/form-data)  
  Zapis na dysk (Files.copy)  
  Download (Resource, ResponseEntity<byte[]>)  
  Export CSV / PDF  

---

### Spring Security
- SecurityFilterChain jako @Bean  
  authorizeHttpRequests + requestMatchers  
  formLogin  
  BCryptPasswordEncoder  
  UserDetailsService (loadUserByUsername)  

---

### Testowanie
- @DataJpaTest  
  Min. 10 testów CRUD  
  Testy RowMapper i custom queries  

- Mockito: @Mock, @InjectMocks, when/thenReturn, verify  
  Testy jednostkowe logiki biznesowej  

- @WebMvcTest lub @SpringBootTest  
  MockMvc (perform, andExpect)  
  @WithMockUser (Security)  
  Min. 5 scenariuszy biznesowych  

- JaCoCo – coverage ≥ 70%  
  Happy Path + Error Cases  

---

### Wymagania dodatkowe
- Logowanie (INFO, DEBUG, ERROR) w kluczowych momentach  

- Dockerfile + docker-compose.yml  
  Aplikacja + baza danych  
  Uruchamianie: docker-compose up  

- Test architektury (JUnit + ArchUnit)  
  Reguły dla Controller / Service / Entity  

- Prosty test E2E (uruchomienie przeglądarki)  
  Sprawdzenie ładowania strony (np. login / Swagger)  

---

### Wymagania funkcjonalne
- Landing Page  
- Rejestracja (płeć, wiek, miasto)  
- Logowanie  
- Bezpieczne usuwanie konta (kaskadowe usuwanie polubień i wiadomości)  

- Edycja profilu  
  Zdjęcie (URL lub BLOB)  
  Bio  
  Zainteresowania (Tagi)  
  Preferencje wyszukiwania (płeć, wiek)  

- Tryb „Dopasuj”  
  Filtrowanie po preferencjach  
  Sortowanie (zainteresowania / lokalizacja)  
  Wykluczanie ocenionych profili  

- Oceny i dopasowania  
  LIKE / DISLIKE  
  LIKE obustronny → Match + powiadomienie  
  DISLIKE → trwałe ukrycie profilu  

- Wiadomości (tylko dla Match)  
  Wysyłanie / odbiór  
  Historia chronologiczna  
  Usuwanie konwersacji  

- Lista dopasowań  
  Unmatch → blokada czatu + usunięcie historii  

- Eksport / import danych  
  JSON / XML  
  Profil, dopasowania, historia czatów  
  Odtwarzanie profilu z pliku  
