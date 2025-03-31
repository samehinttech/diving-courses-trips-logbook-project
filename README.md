# DiveXplore_Project

A website for managing dive courses, trips, and digital dive logs, designed to meet the requirements of the **Internet Technology module** at
**FHNW**.

[![License](https://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

<!-- TOC -->

* [DiveXplore_Project](#divexplore_project)
    * [Analysis](#analysis)
        * [Scenario](#scenario)
        * [User Stories](#user-stories)
        * [Use Cases](#use-cases)
    * [Design](#design)
        * [Wireframe Design](#wireframe-design)
        * [Prototype Design](#prototype-design)
        * [Domain Design](#domain-design)
        * [Business Logic](#business-logic)
    * [Implementation](#implementation)
        * [Backend Technology](#backend-technology)
        * [Frontend Technology](#frontend-technology)
    * [Execution](#execution)
        * [Deployment to a Paas](#deployment-to-a-paas)
    * [Project Management](#project-management)
        * [Roles](#roles)
        * [Milestones](#milestones)
            * [Maintainer](#maintainer)
            * [Contributors](#contributors)
            * [License](#license)
        * [**Note**](#note)

<!-- TOC -->

## Analysis

### Scenario

As a team, we designed the website DiveXplore that offers dive courses,
dive trips, and additional service “digital dive log”.
A new user visits the website as a guest, browses available dive courses, and
books a course without mandatory login. Similarly, guests can explore and book
dive trips without needing an account. Users are prompted to choose a valid
option of the dive certifications provided on the web when booking "dive trips
or advanced courses" or being non-divers otherwise, ensuring only qualified
divers can participate.

Users can optionally create an account to access the digital dive log service,
where they securely log dive details (depth, time, location, notes, ....). Once
registered, they can view and/or edit their dive logs by adding, updating, or
deleting entries. Additionally, users can delete their accounts.

On the administrative side, admins manage the platform by adding/updating
courses, trips, and general content. They adjust trip availability, and maintain
user accounts (deactivating suspicious accounts or resetting passwords).
User autonomy is prioritized: divers retain full control over their data,
including the ability to delete dive logs permanently.

### User Stories

#### - Admin User Stories

1. As an admin, I want to add, update, and delete dive courses and trips so that
   the platform stays up to date.
2. As an admin, I want to manage trip availability so that users see only
   currently available trips.
3. As an admin, I want to edit website content (Terms and Conditions,
   Privacy Policies) to keep information accurate and relevant.
4. As an admin, I want to manage user accounts (deactivate suspicious
   accounts, reset passwords) so that I can maintain platform security.
5. As an admin, I want to ensure users retain full control over their dive logs
   so that they can delete or modify their entries at any time.

#### - Guest stories (non-registered users)

1. As a guest, I want to browse available dive courses and trips without logging
   in so that I can explore options freely.
2. As a guest, I want to book a dive course or trip without needing an account
   so that I can make quick reservations.

#### - Registered User Stories

1. As a user, I want to select a valid dive certification when booking diving
   trips or advanced courses so that I can comply with safety requirements.
2. As a user, I want to select "non-diver" when booking trips or dive courses.
3. As a user, I want to create an account so that I can securely store my dive
   logs.
4. As a user, I want to log in so that I can access my saved dive logs and
   personal details.
5. As a user, I want to view, update, or delete my dive logs so that I can
   manage my records efficiently.
6. As a user, I want to delete my account so that I have full control over my
   data.

### Use Cases
![Uploading Use cases Diagram.png…]()



1. UC-1 [Create Account] – A user creates an account to access personalized features. 
2. UC-2 [Login] – A user logs into the system.
3. UC-3 [Manage Dive Log (View, Update, Delete)] – A user can view, update, or delete their dive logs.
4. UC-4 [Book Dive Trip] – A guest or user books a dive trip.
5. UC-5 [Select Dive Certification (or No Certification)] – A user must select a valid dive certification when booking trips.
6. UC-6 [Book Dive Course] – A guest or user books a dive course.
7. UC-7 [Browse Courses & Trips] – A guest or user can browse available dive courses and trips.
8. UC-8 [Delete Account] – A user deletes their account.
9. UC-9 [Manage Courses & Trips (Add, Update, Delete)] – An admin manages courses and trips.
10. UC-10 [Manage Trip Availability] – An admin updates trip availability.
11. UC-11 [Manage User Accounts] – An admin manages user accounts.
12. UC-12 [Ensure User Data Control] – Ensures users can manage and control their data.



## Design

<!-- Repo Owner Notes: 
 Keep in mind the Corporate Identity (CI); you shall decide appropriately the color schema, graphics, typography, layout, User Experience (UX), and so on. -->

### Wireframe Design
https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=DiveExploreWireFrame.drawio&dark=0#R%3Cmxfile%3E%3Cdiagram%20name%3D%22Page-1%22%20id%3D%22c9db0220-8083-56f3-ca83-edcdcd058819%22%3E7V1Zc%2BM4kv4t%2B6DY6QczcPJ4dNmumd6tmq7t6t7uftqgJVrmtCSqKcplz69fALxxiJREUpSLdoQtgSeQXx5IZCZm%2BG79%2BvfY3z5%2FjhbBaobA4nWG72cIYYwJ%2B8db3vIW4Fg0bVvG4SJthWXD1%2FDfQdYIstZ9uAh2tROTKFol4bbeOI82m2Ce1Nr8OI6%2B1U97ilb1p279ZaA0fJ37q7w1f1%2Fe%2Flu4SJ7TdhfZZfs%2FgnD5nD8b2l565NGf%2F7mMo%2F0me%2BIm2gTpkbWf3ybr5e7ZX0TfKk34YYbv4ihK0k%2Fr17tgxcc2H7P8uuQtf9EZ%2FvCcrFfsC2QfxeGPhothm4tZx%2BJgk1QfZ7rf3%2B%2F9t%2Bj2%2F%2Bj8j5f7LwG1YbBa3GDopPd58Vf7%2FDHyc8XoBPw2gD3323OYBF%2B3%2Fpwf%2FcYgpXutlyBOgleJZOWbwmL0GCqDaB0k8Rs7JYckdLKRe8vJiHICfyuJS%2FOzniuErWDXz0C1LB5Qjg77kA3QEYMFAWwerGDBkJl9jeLkOVpGG3%2F1ULZ%2BqA9nec6nKNpmg%2FivIEneMjbz90mkG2L%2BoMMDzN4r2sfz7Cy8cGzf9YMnsqBzD8KbnFH9eBkkpnOwnlJxsPKT8KX%2BAucMuOntDg11ZUgK1uRDuvB3z8X4siNbfv76dcnFn%2FXI2HWXsI9WLCTRh6dwtbqLVlEsboqB%2BOEXJnH0Z5AfEWJBD%2F46PZ%2BiTZJRDqLsu%2B72%2FipcbljbnNEsiLX8nA%2BBzEtGxkF1tsn5o8IzTO5pmIZ2wDEKAZGGgPaKj3jIPiwT0eW0gY9RjbT2X%2FsoP3CzE6N5y06A7vZVXMYUiRhR3hgvH%2F8mesBeEVQ%2F%2FJA%2BIbtV%2Fsx7htrf8wezTqXPrr%2FPbutvtO9TeeyKD9%2FNwo%2F%2F%2FNuBNxCf%2BHHopp%2FL%2Fz%2Bk7weMfSw1003luQy6m12OEk3%2FtuzMoNK%2FtC%2F1%2FrHmCg1O4aoq02SsoeWXZu6rs4cnfiQ2IgbGUznGKEWaWajGJNTyqj%2BOhmVADyzjGlmmDSIbGEEBGsmApuMQdvxTtDTCaJSQ6R0i2VECvDo8SF3qakwVrdQlqAtLRemPZwTRY2u5K6MK2xmYKh8MqKIGVP1XFHIE%2F7prkL2s%2BXECmjjqYQloKq4AtEj1DHsgSZVP%2FDQoGxJUFWRrHqief8fu688TGYcKYyjY%2FK5xCAF1dULtUuDTTVmPxF53avKXmDs5Jtzo5vGYWrSKEFpTlCqiXEneeQMBCh8PKBNKDFY7Bx1IkZb%2FM09P2NE7NnHfBd3iqtuZsASuJ%2FGTWqrzcLP8OSMZNrmMehFUnqc13N9ygaXKMBmirsZO6wByBn%2BSeY58UVvt9jHaK0qy2VhL2xbhi0Y9X64zxUvEjVr%2BoEFQ69eVyfUGd6ye84xs5kKtXXqAyy5mpg7gRcyIEmfeCqqQlZFoIX5mTb7CAXyCkLZ3CvZCEVXkfShXYBD4cc2XemQa5cNfYYBs3BBRFdcT4L86OhSscgwLpXfJ6dRSdbWnU0aYG2RbLm5LG6c8uVvyqKbQw2vmUgTJM%2F%2F7KwNw%2FM3nAEW2v%2BYjtHncbfUa4LcoXi1Ej1iHOBQlD7DwxsouS8U%2F3IX0NXOwv5k%2FcyDUYGVX2HEVPCVnWDHHs6xrOTb2AMXAdW3bcWsc7FAmS5EDCKXYxYRoMAMoYVaNy4wb6mFmFVEVQchxLMDOw67rQYfdpg8wEQVMdzF7AgL%2F2u%2BSiP2PFmGU6vWFvw0f9%2FyYP%2BfKnRmRq3AX8gbuN%2BKnBMtgl4jLA75ghcBfe39tsf%2F3TG%2FO0xdhx1YhP8Z0Kfu7Djl1ojjxZ3zp2n8JF%2FyTn5Q3Yd3c7%2Fhd%2Frlfrfx1fpuFEA7Zl034yNG7XyVxOA%2B5Wc6o%2BRzO9yt%2BN2EqFc%2B2eoOnWavokCmBl6MvnPur26w54WuNnXnZT4I4dW1IELSxy9CMaxhnJoblAECIjTGgjpOHKFRAzpBrOR5hkhA5ELo21mAcYMv1HGJTD1L2tw9vK6QKxuUpXP%2F2RpWMtqoPP4ofHYIwzqZ4plXK%2FgHxVjkqSTHd8n5ma7Zef84e%2BCUKxYwj90zUjSKm9iRbJ3p62nHxICGjeO8TwWKbwJJ5kYaHynFCZSDSe%2F1RvjCn%2BiW9dr5PNPRXaH5UsI1CvYJxJRFw%2F5H%2FFlfkIVJs%2BNgrYcivOH2yeJi8FLSecfTlaCHE7Ghp64pwsoX78r%2FeD%2BEZ3BB30fYtdYoxM%2FgOzW4ZngH3O3Njo7CCuSFyKzoU83N3AvC7IH4JFpbJ3DauDQvw3OxS9PB328aByUXCJ0F1i1sXODDAo34J4rUwA8V08C7aLMIkjDa799nbL3H44s85t3yJViH%2F0M7hyxg0qYsCxXcgW3zrcLEQ8WfNMUzdyoFc50qeCIpUuVC01UP6ehMLOse%2FNNKrsOKpy%2BUm4MthVcl5SCbbDv%2FtbVid5mElumGFfQ0rajGqQzjdCl14pNPN4O85K6RVjsxr7YWzQW9kUmfnCpk68bsVFmUHTrcGIhzA4wH7xKIORS6FlFmHBLotKYMc14LUA65DmQ3pEKcvOqkzzNIl99OeOym%2BzveP3BPBJhPhZtnf3PN8T1mX9CuiE5CFKfIIhI5NHEDr7jKIkGUDj3jQY8cppUihL2Z3gC5xHcxOo45uDdBF3F%2BBAbaBCzDKDfnuia3OECaX2ZW6zHrAOiGuRQDGNsSEUCx7hj3L9jB0ITMCoONBFek2JFbBJi7COscwj260mSBkzGJTbjv0BHWsmwyn1u8zPjtCXLbEPwTLJbPk4ua13%2BrDx2d1n4Qa7FpM8hEbZVKuHocDMbRcQG2GGpsy6WZrVhTYKdSzbeym90EaQ4VYzNKltkcc5MHcW9M9aszBXz2g5nbxwmQJo9B3iBqXCQLACMq0I%2BXOWEmtMrMJejamHgHAY8CyFdBQJqtKnYqhJpqLB0u7NgQQAWQ7Dnb7MqKwGlr%2FiVlQQndtd%2Ft1RX3thDL011zVcR06jzY7Zv4ynceP%2BotwG%2B7mqZmVKk5%2B0i5INSBvDPe7dcS%2FJsF6m2rEzTxchIu9QKPQfSv%2FMTXghEIVj%2Bbf1v5yI7TtKmSaWtWLZigZdNYYgEQoN5Qhm8pQwGaiUArKcj0LOcyCpoRhBVF1pYfdwWKX2S6wHSZnXF1UoGc5tue61MNM9hDcwWqm3mOghj1PMBoIRg4DSiZIMGKyAnYPI%2BIxRcjMIpcnDsNi3bx7GLXwkHSfdHvK5JhPcj3EzU02KDbKk6ZbTI6hxawJGzkYM0syN0W7H0l1ctzova079n05LrcWnpt695%2F8dbh6Sy9ZR5so8%2FxqvP8gDwPmLHaTcdWteA0%2FTiqHOA1vGH9n122iYuqg%2BpCpnLxP2QjxVpF6XnzLR4ymUyvK%2B0yF54nyUaFsoJvOhcW5OXJOug0qb5MSpjiSSUtayEtaSEyayUyaS03KCUElyUnrspPm0jM%2FWUhQmspQWpGitJSjtC5JaSpLaSlNaSpPaSlRaSFTaUWq5n1MIVb0UUhX9i1lUN4MxdcUDfy7kLK8qS5n%2BaFM0vKDFZ7nRwRAeHsuGXgjEC2V4U5FQBuCFocKWpbsT7nULc7ENi1v81a2uzy%2BOm9PRUJ5d7sETC4ZygtxeYx7J%2FL2ZeUFZKiJrwXeqo11LsjOU9ilXSbjldrqxEVWaqRn3oH6DA9DZNmEnQWQ5wAIVOU4JludHHAMQK1El4VtJX6W8W3MJv43wnlTldDnppIdO8dE5jyi3Tx64Z4JoI2GPCJEnQ9PvXF77HiZunl6zr6p3%2BcYzmFHlrPVPM66YNTtEfJDJxU4uZ9Wwqv5zKRHsBmNLyiVFMRx2DTKdRwHOYqowBhaBLnUgdlfosoK17F47KnLjkIXQqc388%2BcMW8QFW3FQbqw8jnc7ULuFj%2FMYuMBdPNUcEAQG9IZekC1QX1loGY4tTBxKQXYdW2PqpMYxhaW7TmEzTE9KBydKqZ5EgskHsCOQzCkIJ%2FEdQ9qcwb%2FmaDOcvBTbN9F6%2F0mTN4mcI8c3MT1rNLrAbx8jbCGbg9D27Y95BJ1%2FRohyZOrkdgQehbFnuNA12Ezfdd2e0I3VV1oHaH7f%2FaMJhzP4Ha328fccT9he%2BTYdpmlkC1Fpiv4ErSx5RDudoIOs0eKOlVVbGNslWKfs4gO24BxCCGAuBA4wOsiEUCf0KuJ1VCI9B6rlnWJjBZhwtqAnR6oaZ6ITiXMphJm3ZUwO4V9MlnTxD8WqP5ozFpCLab1KROe1KaQCeTeuMm8cj9VN%2Bsxuf849OQ2J1OZNeTUbU7bctkkCXmOx1d3mNpV9DKT3I3Qw8wsdYEHeA4gwLS35TaGaiP0FDfaVBPtCuDpkTq6NPhjs3U6GtFnntFP1dLeJ0IP1EwbDSyxeSo%2BFVIbH6REObUKeOoBMMSiOtTUsmrHYwzm0Ts9l1wjGSjL%2F%2FoZVf7svYRJtQzbvhfhN8bSbH1AGAE9BjMMexYDHgU2D%2BIDvMiNKj8lHgAadw%2B2LccVgsjm7k6cL3f3gGI0Trtyqt%2BmaXvH9dtO4kXGIjVehMey4ohMmSJn4aorK5yUvSji8FXi6CuOe5bbwV4dehJ4OmtSIsHQydSnLEkwfrTKTBfokrrrgzpswCEF7D%2FTLBipdhavD0%2BqWVYqIeopVv0ly7jnTzynuhfDPGqqe9HGmr7S%2BEzMpLSHCC5iTI6VKUwTW65XLOID3ZYkTKq4LvEI8mymbh3YV3ym06JwQLBZ3PJ93ThhVv5uF87rVKuPOxvb%2BO13oQr4EnDW8IdoADT%2Ffv9aPf3%2BrfrtSxCHrHccAGnja5ikN8Quzr7z%2B92wG6Lse3k%2F%2FuWt8kW%2BW9q9szfccvQYObeiFZBLvEoVI9J3yq4qaa%2FeiNjUcpBpcuYo25WkO4cpN2aE998qp235CTtzBwrLoSjI0PSa9oHz2Yf0BUqAd1C6y1EXr37aBlw6g7r7CnwR9WvNUyndLObQ1ISj7pP%2FGKxOFXtMj4b%2F9h%2FF%2FTiYM3Kwm9MPM3p%2FSOxl2zVmF8%2BKKiaN4jAVEUZxyCcebn2%2BcZPR9ExmuIHA0d63%2F%2Bpujm6R6ZwpSCtt1FAQH0uVDqGmOpAhysLtq%2FCMozNKO%2FaatakPVMzcGuKl5Co3uTvNFCMjc%2BJjlCTReqYkWWbuONaA5BAc0hX5peJQ0KGWOpN3oG6uaPdFf1edKGZVv8Gcpzh1C4Uk2n7YMwJsJDhsKzr%2BQJiURHzXdh88bd3MAk0SLO4fHtCDncvhin%2BgTnH3LPMzZamDsSBFGbWaqzSnfOf7eWpf0zN7%2Fc%2BM%2FSyiOuZ%2BMgXsD591YgJg7veXskyQbWl8Vq4GoYXC6l4MDRCrOakhcdSmbdSQVkL1qIbU6M5JDZ2nhtzmkMRRqCFz0YAB1dCZj6posxZr5ShfKxcHvmVjzw8RALT3v17lqB3z0WlME6%2BMWWPqIngnjdmHxvTkefsoNKbq4Zg05pka0%2BATHpvGPLokw6QxJ43ZtcY08MqYNebk6hxIY5bLXmPSmJqYmEljnqkxr8TVeaBO7aQxJ405jMa8Pq9sUQJgSqE%2FZ503X%2Fdrsctvb4Q0J11O2fNT9vyFs%2Bdh88Qb5lF6g%2BxRB6FurjDlx18oecKEjzI%2Fvi5vdbme2hB8JYquwzRjcz6nkhAzpblfAco8olHjPO1mSLmU92ZKXv9ucHcoeX1Q6KHzodeBSsyfKWcHyxnr%2FeQGvzNspVnsNeWpIk23AXSPMNMlD3afjK7dYMBgfqmJ51OSeVuEISB5gjVKtALCmnXWH8Z0STMjsM2mVHFN23eXKt7AUa4hw%2BiStgG%2ByB46x7rhPO3EUO%2BJI71NDKnZtXDmCsF%2FB8FWWEJM67H%2F0RP780ck3PS%2FsqGPv%2FmJ2NngdvHCxnAfv8OKvEbnTDv3HTC7FBWDoXAm6q1avUVxTWNpdleOJPi9if%2BlVEbPUyUlJhrmLyze7pnfNnuFOloe1Kpblbu7yIYcM7F5Brcq6wdePrN7S7ERvTtM4YJJf90wtuZ8%2FnUbZLs%2F%2F8jIkIThU1jbTfU62NqWZxRQt742NKXNzpEzKU1Be0rz%2BSon76douRSq4ZEPXLHDhLLFxHUQXGVtDcG1YSZ9ElwbhFkrjZIFF3XsHWgTYYbuMcZuM%2F1MQUYmd4IxrV1ZRjfHmNldwcJx6o4rxyOaiAkH6iZCGFn57pk9QKNF7vmRJTmKAhqEzOr1M%2BDFCmi4evKcWTOASrNbORqwdf0MZfbnSDO6jgpmUKB9YeN7Ife88%2FN%2B9FphA9rqJFUqsSHWpGVUV8pjyJUuqpP7YypmpA8xFb84ycFi9wPd%2Bp7sRdRP%2F8UuUF5yewrTOiZIWTYkLx%2BmhYB58WMK05rCtC4bppWJmRGFaSFgXsiZwrSGXrgw4mPEYVoImNPPL7p6NoVpnYiyMYRpIWBO0ZzCtN4n7kYSpoWAOddp2kdkfLAZXwQWAuZckSkCywS3y0VgNSFsjBFYCJiDIEYRtFREj2pDsppjS6cwre9dsI8wTKt4hzGFaSluOKhRgAY%2FnOzS73CkdCv3YxspF7T3WPaHqcn33IXvmbQGfX%2BYn5zPk%2FN5tM5nODrnM5yczyMyx0z4GLPzGY7U%2BazMgnTe6NMmQRMUx%2BGhhpOH%2BnvD3Vg81NqiQpOHeqywGaGH%2BkA1o8lDbYLbBT3UDQgbpYf6QAWgi9pmU47w5Hxu5KgxOp%2B1BZEuLxavzFEISftN43qbtx6oQjR5CidP4YU9hSmDjMlTmEu6yVM4Bt1pwseYPYUHqiJd1ICbwlRPRNkonIAH6iCN2QmouKcNXkGdh3ryFLYA51g8hci8PjJ5CscHmxF6CtEJyxyTp%2FBynsIGhI3SU4jM6xmTp3DyFI5bZo%2FSU9iigMbFgy8hcTQWkqGYYH9D1WJPsyNrjWzYi6XFRhzg5A1%2FiIbye1ltRHx7q347td5I5wUZPMxeGRAKbIo9l1knNQK6ElHaVhZBtgctJqm8%2FKde98HBshego0IjvDf5olT2rBtyuHgIsl2v4Zp%2B6ocgrHqI8%2FohVbUEvuhKJFXLiFRw2r5miFx7JBtadnP6YUbvD0kEU6GRFoL28P5OHIp55ENOigwoZ8I8j8CW7jpA3RHcfaWjUvoAiOvSh8I%2BpU9DtaOMiVJePjQmaRR157IMUqeuf7Bc26y1%2FKL5BrK5xMprmXUssJjdUH%2FlG9hQ68h2j7sA0XMvyEex9QWOM4j4NJdfqnhcm6XneIswZbLjDD45z3DKTbJeirQhb1ZWaWOSjM6OrdFWyEFe4w2TuiB0vAsKwpynmyVhT1YdxJIlrlTaai8JqWzTy5OarkRhsXtK%2Fs5NkpCAY84vpxgnng%2BJPYRcI2azsO7Dv2rRRgxT7jOBfyPVQb1Bkt7u0dIjSCMup3Knw5U7xVDhcN3CtJ0Dt%2Bb6xL2tSxPzunRH9czbuukxMq7e7Oa8UPEsLV35uxxEY3AeThsiyMFY04YI170hArKlkrhIE4NQlLOtbYggW1gdChBzCEJHAsTIAZIAQaYbOXzEAj%2FeNePgTKFRV6hXjy%2FsqvgauDA%2FIuYQgrHgi2tnJt%2FkVeF%2BIDYiWXodoHZlj9EYQG2OahjY6jKCWugRZk4cCpm5EKbfmZSleASANIdAjAWQPBSJSS02edzPkygeHSr1kvYK5aU3Qnia80XPhOfD2g%2F582sxNgeA8xtXib5QjM%2BB%2BJdE4vNqy4cs2nMMrB%2FZHIt9iDZpG8fOhpOYadrqPn5%2Bvo8fQwn4OfDnz%2FwioYzFXfc7MewJb%2FY3nEh%2F7YNdEkbC4eZn67Dp%2FRfhSwrLwhDhyFwyszf9JODNZocvwUrgYf6nvwx2%2F6GHp9Tpg7E7usuAcLfXCFL9BBZ%2B4t8Em0XtDLGVm3zSLvHjpH6aoL3JlkviSIxC50%2FIgXK7WMQB79tt1dTLHts8OPuV6eWIB1u9HIHm7q%2FCU8e%2Bg4efTPe2z8bqsz8KWC%2BDTRD7Qoxs%2FtqHMd8jTXB9%2Fmrcfo2517lyv00Ubfl1tWer7z9n3BTFN2KBQDr5rN7wNVu5N%2FPMhU44Nwe%2FbwPWk808sObRuoIpvw3OWNtKSeU7GR5iWaVFp7wDrHkqPFo%2Fm5jg8RhFf6ay0d8sBBJ2Qfzip3L0Ujhp2S2CNN3KOrQbIVDaoZ%2FQHuRI62cb5chuv2UmW3I5TLTsgU525O%2FeFyR4Os5qdg0GrLz0CilW11mwbrtBIm9W1qEF29um0Xf%2B6jgD9j%2BF2eq%2FMHtGLJdmxiaPj9glhRFbMztLtZoZkjna8rPXYjV9EwQLw4TnXIuym8u0Az2ShZRy%2Ffyf%2B%2FVjEOdS6AO3uQBwbP5XOGUw360doErXH1sOx2n2GSDqSapUQjoFnD%2B7wS4%2F4wn%2F4NOd1Bo3uYX01vnYF9Y%2BR5uF4CrBnR%2FjkH8Tj%2FPYPwGE28%2Fsz82MF%2FzPWr58rva7ca1rBMAYORUa31PouZtdquj4a2zjwHQnniUiQbNOIgnBoEXTVz%2FZxwU0IFCwQSZsfLfY2G8KZNytol1ts%2FAJAd0hQGuPXHOoAQbSqpnWiEY6NzCyewtWyoM7uzei%2FzdMPf9treh2dg3h9YHbuC6o%2Bkbppu8%2FPT2F81rFIK0tU%2BIP8kIj4LMvYsy%2FJnGQr2R83TMoCQ0B1au%2BbuNws3wK2SinZ98Ks%2F9r4vNrREkkhPlK%2FHicxR1Il1FPB1rYtYrVP3ZxaTZp3cmkncyWyaSdsDGISaupjXAthDnG0jxUKmFMtmbhdi1tTTXkAOkS85Uc8A5tTXPcYcuSf75M%2BlpxmbRwxpO%2FDlcZnNfRJsoYqzhexsXklivglLvJEhhuxWsICVQc4qS7WUeL7LpNVJSgUDFFBUV4ysZndsGKNzyI1jhiZxbf8iGjYtBYyz3%2FzJmT8lGhbKSbzoXFuTkyTroNKm%2BTEqY48oUJ9G9RvChPSOlTnFDNOuGNoswH%2B1DJKqk3F0kl%2BS2wI37E0Qq38MNijHl7ShjetAqeRCelLBR%2BKMtDyZ%2FytXhRULZxwvMmvkkNLRNURBMVTZWhSNmvzWAXh4pxLhmScpYsh1qso%2BT3eSsPeFTUScyPpDxaPoBWrsp4tThYvSNj2aJ9WXkHGQniawGHamMdpNl5CpoNUlKSh8OXI5HqtrgIadKBiG6GTRynv0KV1LxQ1TqEz1SHLb9Rsbj4SYQa%2F%2FLMJ6YPrzyxR6zKs28fgiWfwhbE061HHiy7pSq49imFzSWlu0AAkQLtdLsBFkUDaoF2sDetlyvZnqnfqkrk7SoO%2FAUfsdtNgQ9OuI%2FvGhdYrl%2BrlwxYmyjYGzBcpAAji4iTxldfkE6tuSXlbGaqVT%2FGOYWESj1An1KZqoXfyaya5QlpT2Lc023pSYcllZqRVRpHE7Vq1NIV8R2YWrp5hkSlIXKwy%2BLu9STrIpn6nUDAkSFANRDQzTVxbxDwVAjk0cZ%2BFm0sI6JtEUOJEPL4njOgTXPznGXOLJqAkawOHad%2Bjx6rJni6PK3%2BmbORulV%2BlUgssYxMcYmL7TvHdg7WUzsSCTrhiXtCgieXRegTCWoGilGpfsfM6XlgOJK0KJ05MeelmBMC2WbqDwpFQakKFH7kT17GQTArYlkT4Wdgc7y1SJ8SuQDzaLMIs4yqfrGDVNvq4Z7%2FHgOgBsQUZcFXvMjSl2gnusaOxSnBC0vsk3S8sMiMploG3U%2FiPVITMm35hcdW3N%2Fw7rEhWIRB7Y1EDx%2B6wzDUQBh2A2ElMRcWVaSGgLBaWeur8BcD7gcbVqwJJLn0412T5ipcJg24%2FCh%2BukOBzivakSBjml5GgVxTrk8UqG4WsdsT%2BHECQX2nCplKhVO0JUw6n8aJ8ACD77RtiRHYlEVfuDt%2FDm7SYeejltuhB72i5%2B8TNnkY2kATebJNfHEPAwaqh%2BHX7UIE9%2FGaamly5B0fwyc2ilwa%2Fi18Yn%2BD13CXsH7%2FoABmBHMco9qGQHWXD%2BvVw0B1HNw9R9GOD%2FjHcKWWWZR44uMD%2F9Vx0a340Qylxvj6CPhvb8OswbVuc6AeYa1WLfknjyx8EiMM5s9swFW92RF0S8nRnWuSNo8w1NV363GIVb%2FH13WacbgLVlzqX0KVUMB%2FiyO%2FZcMDR6Rc0DDuawYH3T5vmu1eesRIC0dMTvAkDv3NUozeIuSkTuefu2jPX16mHmG6kzomBt1WCmRnTa2G%2BBDSjSMPa8MOtLvrqYPezQYRN%2FK%2BE8pGpT1OSaA6Mf17wKvYqDT%2BzhcTPQ0myKBmB1Sr%2BU7SelTSml5cWkPdNPXqpLVpl5bvXlqrM70PYSzc3%2FdivjfJbIkhNSb2sEJbu8WpRKajwlMVAVwsC0gMe%2F%2BR%2F2oEs8jdgGLm2NG4y5vvFQuFzZvv9ScJzVVYCwdaIAIxCAhe%2FfV2JZfTCSeH2iVCdjRTHnxobbMHnj0hSlraVNVx03yQ8r9%2BR1XPVPcn2r6lO%2Fmy4blDs1v2SoCnEM1QVgKKh8vyypK3okNiCXA3y8ufpZV6tKmlwyR%2FDfWoX2rrvXeV9d732Nsvcfjizzn3fIlWIf%2FwTsKi%2BfaZNSmAgGbFh%2BqiovsTA7hF8OYqrLgYcxUrLNKqkj2kvm2H%2F3Y1jG6LYSS6Yewt6wBrdsubpOk45cskTd%2BnNIVFPsnlpGmbLR1HLk0hvrw01ezR9%2F7nk1iXdzPwfPLAVjWTGhuXYJ%2FU2DtRY3Kt3zGosRb%2BvHGpMQwkcWo7l9diLbamH9coKsbAGEZxmlldi5CeVNI7UUm4WQoMrJG0dTPf%2BYzA1i3sDTwhoOjadNgYLQGq26J61KNYpkEeMkuHHsYWvuorFwIQy7J3DELAvMo8mWDjMkomE%2BxdmGDQtsdnguk2WB21EoMOHJ8poHNtT7J0jNJlkqXvQpaW2YsjkqWTU%2Bta%2BGKSAu9CCkC7zTr3wGJAW6v0nU9oR7HObU9W2LVIpEn%2BvhP528ahOLT8vTrvNnTahAsMO6clpEU%2B3rnZJGkyiVLdJ9Nu2hywZlzL%2BZOmWmQKL%2FWhGB2iSfHT68W%2BSueTA%2FFfSkGettXUoZvv%2ByFpUP3mMVq5ec8VolEWtt%2FTRNrYxvAGxfY2MNPt5f8f9Eq%2B7GO7zXbk%2Fgld36B9zk%2FWals2pJkN63xSZnFJmVQ6DmzFOofkjMpRdYYZ0I4k2kCpo7bYOcQFCsqa9iXYV%2FmFXSAKrxXo2WsQVQfaKEE1EIiy%2B8hbFzi6xGttPVolR7pDmOlc78UOkC3Fsow7bGdwq3ww4I4acPdfkdiks7blqE40z2o7PU4o01ftBzBH0EDCSxefODyIKkhutakKmwIm%2FlzZ6lZhhKYp0%2FeIOwioq60kMTT0zvd%2Fd6E3MzX5SxxuzSJsgg3CmFr5pCPTiyqIdIXz%2BkPQgY20W09MyGGjXbv15QEY3fH9joNugdTtjFhCU1FlIat48HNGOGzyNPeHMCRnQ2n0YwWEg5Q1IQfW6i9qdt0%2BRntF%2FzXbXcadYy%2FameIl4kYFflDXH9oq9nuU2a5SJ%2Bjyar9NHGywWdzGsSCR2OI6nM9q7tW6By94DZPf%2BWcLIDv7%2Fof4bts4%2B37%2FWjn5%2Fq3y5UuloFfp5w4Wy8BIi6xpx2T9PDjQV5jXREr8eBnknuyFY%2FuuHzyRBZ17EN4YSoCdWb5LnsLKWwynL59dVNJULSQvLZnKjvi0a8p9GPn8t8ppW37C7ujXNb0V1L6UuQ%2FugdPZh%2FRtS1B3UMKMUNUh%2FtM24GtWQLYVZPQz7ImdLTjSAyY%2B%2FceiTlF1%2B9Zj1nfSh8yjzYbJp%2Bx%2Bs2JjiJNkD%2B0HuNIS0Q2CHWyUNcu22a2cXm6wix%2F%2BHw%3D%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E
<!-- Repo Owner Notes:
It is suggested to start with a wireframe. The wireframe focuses on the website structure (Sitemap planning), sketching the pages using Wireframe components (e.g., header, menu, footer) and UX. You can create a wireframe already with draw.io or similar tools.-->

### Prototype Design

<!-- Repo Owner Notes:
A prototype can be designed using placeholder text/figures in Budibase. You don't need to connect the front-end to back-end in the early stages of the project development.
-->

### Domain Design

<!-- Repo Owner Notes:
Provide a picture and describe your domain model; you may use Entity-Relationship Model or UML class diagram. Both can be created in Visual Paradigm - we have an academic license for it.
The ch.fhnw.pizza.data.domain package contains the following domain objects / entities including getters and setters:
-->

### Business Logic

## Implementation

### Backend Technology

### Frontend Technology

## Execution

### Deployment to a Paas

## Project Management

### Roles

### Milestones

#### Maintainer

- Iulia Mara Udrea
- Mehak Khan
- Sameh Ahmed

#### Contributors

- Charuta Pande
- Devid Montecchiari

#### License

- [Apache License, Version 2.0](blob/master/LICENSE)

---

### **Note**

*Built for learning, not profit — but hey, maybe one day? 🌟*

*P.S. Open to feedback! We’re still students, after all.*
