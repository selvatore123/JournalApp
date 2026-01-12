package com.example.journalApp.config;

import com.example.journalApp.services.UserDetailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SpringSecurity {

    private final UserDetailServiceImpl userDetailService;

    public SpringSecurity(UserDetailServiceImpl userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/journal/**","/user/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(userDetailService) // btw its auto detectable due to component scanning provided by springboot so if want to remove userDetailService from this class we can remove it, Spring automatically detects it from the application context.
                .build();

    }
}
/* Good question! Let me explain what `userDetailsService` does and why it's needed.

**The problem it solves:**

When a user tries to log in with their username and password, Spring Security needs to:
1. Find that user in your database
2. Get their password from the database
3. Compare it with the password they provided
4. Load their roles/permissions (like ADMIN, USER, etc.)

**Where does `userDetailsService` come in?**

`userDetailsService` is the bridge between Spring Security and your database. It implements the logic to fetch user information from your database whenever authentication is needed.

In your code, you're using `UserDetailServiceImpl`, which presumably looks something like this:

```java
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Return user details with their roles
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),  // This should be encrypted (BCrypt)
            user.getRoles()       // Their permissions/roles
        );
    }
}
```

**What happens without it:**

If you don't provide `userDetailsService`, Spring Security has no way to know:
- Who your valid users are
- What their passwords are
- What roles they have

So authentication would fail because Spring can't look up the user credentials.

**The flow with userDetailsService:**

```
User submits: username="john", password="secret123"
                    ↓
Spring Security calls userDetailsService.loadUserByUsername("john")
                    ↓
Your implementation queries the database
                    ↓
Returns UserDetails with encrypted password and roles
                    ↓
Spring compares encrypted password with what user provided
                    ↓
If match → authentication succeeds, user gets access
If no match → authentication fails, user denied
```

**In your specific code:**

```java
.userDetailsService(userDetailService)
```

This tells Spring Security: "Hey, when you need to authenticate someone, use this `UserDetailServiceImpl` class to fetch their details from the database."

**Quick analogy:**

Think of it like a bouncer at a nightclub. The bouncer (Spring Security) needs a list of who's allowed in and what their VIP level is (roles). The `userDetailsService` is the person who maintains that list and provides it to the bouncer when needed.

*/
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------



/*
If a database is stolen, Bcrypt-hashed passwords cannot be "reversed" through reverse engineering because hashing is a one-way mathematical transformation, not encryption.
Blowfish cipher -> Algorithm that Bcrypt password encoder uses.


**BCryptPasswordEncoder - How it works:**

BCrypt is a password hashing algorithm that makes passwords secure. Here's the key thing to understand: BCrypt doesn't encrypt passwords (which is reversible), it **hashes** them (which is one-way).

When a user signs up with password "myPassword123":

```
User enters: "myPassword123"
                    ↓
BCryptPasswordEncoder.encode("myPassword123")
                    ↓
Generates: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO"
                    ↓
This hashed version is stored in the database
```

Notice how the hashed version looks completely different and much longer. If someone steals your database, they can't reverse the hash back to the original password.

**When the user logs in:**

```
User enters: "myPassword123"
                    ↓
BCryptPasswordEncoder.matches("myPassword123", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO")
                    ↓
Compares the entered password against the stored hash
                    ↓
If they match → Login successful
If they don't → Login failed
```

**Key characteristics of BCrypt:**

It's **slow by design**. This makes brute-force attacks impractical—even if someone tries to guess passwords, each attempt takes time. A regular hashing algorithm like MD5 is too fast, allowing attackers to try millions of passwords per second.

It includes **salt automatically**. Salt is random data mixed with the password before hashing, so even if two users have the same password, their hashes look completely different. This prevents attackers from using pre-computed hash tables.

Each hash is **unique every time**, even for the same password. So you can never directly compare hashes; you always use the `.matches()` method.

**In your code:**

```java
@Bean
public static PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
}
```

This bean is used in two places:
1. When a user registers, their password is encoded: `passwordEncoder.encode(plainTextPassword)`
2. When a user logs in, their entered password is compared: `passwordEncoder.matches(enteredPassword, storedHash)`

---

**AuthenticationManager - What it does:**

The `AuthenticationManager` is the **orchestrator of the entire authentication process**. It's responsible for taking credentials (username/password) and determining if the user is who they claim to be.

Here's the flow:

```
User submits credentials (username + password)
                    ↓
AuthenticationManager receives the credentials
                    ↓
Asks: "Who can handle this authentication?"
                    ↓
Finds appropriate AuthenticationProvider
                    ↓
AuthenticationProvider uses UserDetailsService to load user from DB
                    ↓
Compares passwords using BCryptPasswordEncoder
                    ↓
Returns Authentication object with user details and roles
                    ↓
If successful → User is authenticated, roles are loaded
If failed → AuthenticationException is thrown
```

**In your code:**

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
}
```

This creates an `AuthenticationManager` bean. You would use it when you need to authenticate someone programmatically, like in a login controller:

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // If we reach here, user is authenticated
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // You can then generate JWT token or session

        return ResponseEntity.ok("Login successful");
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(401).body("Invalid username or password");
    }
}
```

**How they work together:**

```
User provides credentials
                    ↓
AuthenticationManager takes over
                    ↓
Uses UserDetailsService to fetch user from database
                    ↓
Uses BCryptPasswordEncoder to verify the password matches
                    ↓
Returns success or throws exception
```

**Simple analogy:**

Think of it like airport security:
- **AuthenticationManager** = The security checkpoint supervisor
- **UserDetailsService** = The person who checks the passenger list
- **BCryptPasswordEncoder** = The security scanner that checks if the ID is authentic

The supervisor coordinates the process, the list-checker finds you in the system, and the scanner verifies your credentials.


Question -> When Bcrypt is slower than MD5 in hashing then why it is preferred over it.

This is a very common point of confusion! You have spotted the exact reason why hashing is so secure, but you are missing one small piece of the puzzle regarding *how* an attacker works.

The short answer is: ** Attackers do not decode the password.**

Because MD5 and BCrypt are **hashing** algorithms (not encryption), they are "one-way" streets. You can turn a password into a hash, but you can **never** turn a hash back into a password. It is mathematically impossible to reverse it.

So, how does an attacker "crack" it? They use the **"Guess and Compare"** method.

---

### The "Guess and Compare" Method

Since the attacker cannot reverse the hash, they have to guess what the password might be, run that guess through the algorithm, and see if the result matches the hash they stole from your database.

Here is the step-by-step process of a "Brute Force" or "Dictionary" attack:

1. **The Theft:** The attacker steals your database and sees a hash: `a5f3...`
2. **The Guess:** The attacker picks a word from a list of common passwords, say "password123".
3. **The Hash (The Critical Step):** The attacker runs "password123" through the **same algorithm** you used (MD5 or BCrypt).
4. **The Comparison:**
* If the result is `a5f3...`, they know your password is "password123".
* If the result is different, they try the next word.



### Why "Slow" Saves You

This is where your example comes into play. The attacker has to perform **Step 3** (The Hash) for *every single guess*.

* **Scenario A (MD5):**
The attacker guesses "secret". They run it through MD5.
* *Time taken:* 0.000001 seconds.
* *Result:* "Nope, that's not it."
* *Next guess:* "secret1".
* *Time taken:* 0.000001 seconds.
* *Impact:* Because MD5 is so fast, they can make **millions of guesses per second**. They will likely guess your password very quickly.


* **Scenario B (BCrypt):**
The attacker guesses "secret". They run it through BCrypt.
* *Time taken:* 0.1 seconds.
* *Result:* "Nope."
* *Next guess:* "secret1".
* *Time taken:* 0.1 seconds.
* *Impact:* Because BCrypt is designed to be slow, the attacker has to wait 0.1 seconds for *every single guess*. They can only make **10 guesses per second**.



### The Analogy: The Locksmith vs. The Rubik's Cube

Imagine you have a key (the password) that opens a safe. You lose the key, but you have a machine that can cut a copy of any key you can imagine.

* **MD5** is like a machine that cuts a key instantly. An attacker can cut 1,000,000 different keys and try them in the lock in just a few minutes. Eventually, one will open it.
* **BCrypt** is like a machine that forces you to solve a Rubik's Cube every single time you want to cut *one* key. Even if the attacker has a list of possible keys, it takes them so long to cut each one that they will die of old age before they find the one that fits.

### Summary

The attacker isn't "decoding" the slow hash. They are forced to **run the slow encoder** millions of times to check their guesses. That is why BCrypt wins.

Would you like me to explain what a "Salt" is and how it adds another layer of protection to this process?
*/

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

/*

Perfect, let me explain with concrete examples!

**1. BCrypt is slow by design**

Imagine two password-cracking scenarios:

**MD5 (fast, bad):**
```
Attacker wants to crack password "secret123"
Attacker has a list of 1 billion common passwords to try

For each password attempt:
  MD5 hash takes: 0.000001 seconds (1 microsecond)

1 billion attempts × 0.000001 seconds = 1000 seconds (about 17 minutes)

Result: Attacker cracks the password in 17 minutes ❌ DANGEROUS
```

**BCrypt (slow, good):**
```
Same scenario - 1 billion common passwords to try

For each password attempt:
  BCrypt hash takes: 0.1 seconds (100 milliseconds)

1 billion attempts × 0.1 seconds = 100 billion seconds
                                  = 3,170 years ❌ IMPRACTICAL

Attacker gives up after a few hours ✓ SECURE
```

So the slowness is intentional security!

---

**2. Salt - preventing pre-computed hash tables**

Let's say two users sign up with the same password: "password123"

**Without salt (BAD):**
```
User 1: john
Password: "password123"
Hash: 5f4dcc3b5aa765d61d8327deb882cf99  ← Always the same!

User 2: mary
Password: "password123"
Hash: 5f4dcc3b5aa765d61d8327deb882cf99  ← SAME hash!

Attacker creates a pre-computed table (rainbow table):
  5f4dcc3b5aa765d61d8327deb882cf99 = "password123"

When attacker steals the database, they instantly know both passwords!
```

**With salt (BCrypt does this automatically - GOOD):**
```
User 1: john
Password: "password123"
Random salt generated: "randomSalt1xyz"
Combined: "password123randomSalt1xyz"
Hash: $2a$10$N9qo8uLOickgx2ZMRZoMye...  ← Unique!

User 2: mary
Password: "password123" (same password!)
Random salt generated: "differentSalt9abc" (different random salt)
Combined: "password123differentSalt9abc"
Hash: $2a$10$K8pp7nHg9Yk0k1LmLm9Pae...  ← DIFFERENT hash!

Even though both users have the same password, their hashes are completely different!

Rainbow tables become useless because you'd need a table for every possible salt combination.
```

**Real BCrypt example:**

```
User: alice
Password: "mySecret"
BCrypt generates random salt internally
Result: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO

Next time you hash "mySecret", you get a completely different result:
Result: $2a$10$SaltIsRandomEachTimeQaZeKeUxWdeS86E36gBSstO1234567890

But BCrypt is smart - it remembers the original salt inside the hash,
so it can still verify the password correctly!
```

---

**3. Each hash is unique every time - using `.matches()` method**

This is the confusing part. Let me show you:

**Wrong approach (won't work):**
```java
String password = "myPassword123";
String hash1 = passwordEncoder.encode(password);
// Result: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO

String hash2 = passwordEncoder.encode(password);
// Result: $2a$10$DifferentSaltGeneratedQaZeKeUxWdeS86E36gBSstO1234567

if (hash1.equals(hash2)) {  // ❌ FALSE! They're different strings
    System.out.println("Same password");
}
```

Even though both hashes represent the same password, they're completely different strings because each time a random salt is generated!

**Correct approach (using `.matches()`):**
```java
String password = "myPassword123";
String storedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO";

// When user logs in and enters the password
String enteredPassword = "myPassword123";

if (passwordEncoder.matches(enteredPassword, storedHash)) {  // ✓ TRUE!
    System.out.println("Password is correct!");
}

// But if they enter wrong password
String wrongPassword = "wrongPassword";

if (passwordEncoder.matches(wrongPassword, storedHash)) {  // ❌ FALSE
    System.out.println("This won't print");
}
```

**How `.matches()` works internally:**

```
BCrypt extracts the salt from the stored hash:
storedHash = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBSstO
Salt embedded: N9qo8uLOickgx2ZMRZoMy

Then it:
1. Takes the entered password: "myPassword123"
2. Adds the extracted salt: "password123" + "N9qo8uLOickgx2ZMRZoMy"
3. Hashes it again with the same salt
4. Compares result with storedHash
5. If they match → password is correct!
```

---

**Real-world example put together:**

```
SIGN UP PROCESS:
================
User enters password: "mySecret"
                    ↓
BCryptPasswordEncoder.encode("mySecret")
                    ↓
Generates random salt + hashes
Result: $2a$10$K8pp7nHg9YkLm9PaeGHDMuIjZAgcg7b3XeKeUxWdeS86E36gBSstO
                    ↓
Store in database: user_password = "$2a$10$K8pp7nHg9YkLm9PaeGHDMuIjZAgcg7b3XeKeUxWdeS86E36gBSstO"

LOGIN PROCESS:
==============
User enters password: "mySecret"
                    ↓
BCryptPasswordEncoder.matches("mySecret", storedHash from DB)
                    ↓
Extracts salt from storedHash
Hashes "mySecret" with that salt again
                    ↓
Compares the new hash with stored hash
                    ↓
They match! → Login successful ✓
```

Does this make sense now with the concrete examples?
 */