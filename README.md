Implemented based on https://developer.android.com/courses/pathways/android-basics-compose-unit-2-pathway-3?hl=en.

Core functionality:
1. Allow users to input an amount in a base currency (e.g., USD).
2. Provide a dropdown menu to select the target currency (e.g., EUR, JPY, HKD).
3. Perform correct conversion using a fixed exchange rate table (hard-coded in the app,
at least 5 currencies).
4. Disallow invalid input (non-numeric, negative values).
5. Display the image of the national flag for different currencies.
6. Show the converted result in a Text composable.
7. App should target SDK 34 or 35 (no lower limit on min SDK).

Other functionality:
1. Support decimal precision control (user can choose 0, 2, or 4 or more decimal places).
2. Add reverse conversion (swap base and target currency with one button).
3. Include real-time date/time display of conversion.
4. Provide conversion history (list of last 5 conversions).
5. Add support for dark mode (automatic theme switch).