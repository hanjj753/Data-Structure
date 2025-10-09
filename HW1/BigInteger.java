import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigInteger {
    public static final String QUIT_COMMAND = "quit";
    public static final String MSG_INVALID_INPUT = "Wrong Input";

    // 정규식: (부호)(숫자) (연산자) (부호)(숫자) 형태를 찾아냄
    public static final Pattern EXPRESSION_PATTERN =
            Pattern.compile("^\\s*([+-]?)\\s*(\\d+)\\s*([+\\-*])\\s*([+-]?)\\s*(\\d+)\\s*$");

    final int[] digits; // 숫자를 역순으로 저장
    final boolean isNegative;

    // String을 BigInteger 객체로 변환하는 생성자
    public BigInteger(String s) {
        
        if (s.equals("0")) {
            this.isNegative = false;
            this.digits = new int[]{0};
            return;
        }

        if (s.startsWith("-")) {
            this.isNegative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            this.isNegative = false;
            s = s.substring(1);
        } else {
            this.isNegative = false;
        }

        // 0으로만 이루어진 문자열 제거 (e.g., "000123" -> "123")
        int firstDigitIndex = 0;
        while(firstDigitIndex < s.length() -1 && s.charAt(firstDigitIndex) == '0'){
            firstDigitIndex++;
        }
        s = s.substring(firstDigitIndex);


        this.digits = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            this.digits[i] = s.charAt(s.length() - 1 - i) - '0';
        }
    }

    // 내부 배열을 이용해 BigInteger를 만드는 생성자
    public BigInteger(int[] digits, boolean isNegative) {
        this.digits = digits;
        this.isNegative = isNegative;
    }

    // 부호를 반대로 바꾼 새 BigInteger 객체를 반환
    private BigInteger negate() {
        return new BigInteger(this.digits, !this.isNegative);
    }
    
    // 두 BigInteger의 절댓값 크기를 비교 (this > big -> 1, this < big -> -1, this == big -> 0)
    private int compareAbs(BigInteger big) {
        if (this.digits.length > big.digits.length) return 1;
        if (this.digits.length < big.digits.length) return -1;
        for (int i = this.digits.length - 1; i >= 0; i--) {
            if (this.digits[i] > big.digits[i]) return 1;
            if (this.digits[i] < big.digits[i]) return -1;
        }
        return 0;
    }

    public BigInteger add(BigInteger big) {
        // 부호가 다르면 뺄셈으로 처리 (A + (-B) = A - B)
        if (this.isNegative != big.isNegative) {
            return this.subtract(big.negate());
        }

        // 여기서부터는 부호가 같음
        int maxLength = Math.max(this.digits.length, big.digits.length);
        int[] resultDigits = new int[maxLength + 1];
        int carry = 0;

        for (int i = 0; i < maxLength; i++) {
            int d1 = (i < this.digits.length) ? this.digits[i] : 0;
            int d2 = (i < big.digits.length) ? big.digits[i] : 0;
            int sum = d1 + d2 + carry;
            resultDigits[i] = sum % 10;
            carry = sum / 10;
        }

        if (carry > 0) {
            resultDigits[maxLength] = carry;
        }

        return new BigInteger(normalize(resultDigits), this.isNegative);
    }

    public BigInteger subtract(BigInteger big) {
        // 부호가 다르면 덧셈으로 처리 (A - (-B) = A + B)
        if (this.isNegative != big.isNegative) {
            return this.add(big.negate());
        }

        // 여기서부터는 부호가 같음
        BigInteger bigger, smaller;
        boolean resultIsNegative;

        if (compareAbs(big) >= 0) { // |this| >= |big|
            bigger = this;
            smaller = big;
            resultIsNegative = this.isNegative;
        } else { // |this| < |big|
            bigger = big;
            smaller = this;
            resultIsNegative = !this.isNegative;
        }

        int[] resultDigits = new int[bigger.digits.length];
        int borrow = 0;

        for (int i = 0; i < bigger.digits.length; i++) {
            int d1 = bigger.digits[i];
            int d2 = (i < smaller.digits.length) ? smaller.digits[i] : 0;
            
            int diff = d1 - d2 - borrow;
            if (diff < 0) {
                diff += 10;
                borrow = 1;
            } else {
                borrow = 0;
            }
            resultDigits[i] = diff;
        }
        
        return new BigInteger(normalize(resultDigits), resultIsNegative);
    }


    public BigInteger multiply(BigInteger big) {
        // 결과가 0인 경우
        if ((this.digits.length == 1 && this.digits[0] == 0) || (big.digits.length == 1 && big.digits[0] == 0)) {
            return new BigInteger("0");
        }

        int[] resultDigits = new int[this.digits.length + big.digits.length];
        
        for (int i = 0; i < this.digits.length; i++) {
            int carry = 0;
            for (int j = 0; j < big.digits.length; j++) {
                int product = this.digits[i] * big.digits[j] + resultDigits[i + j] + carry;
                resultDigits[i + j] = product % 10;
                carry = product / 10;
            }
            resultDigits[i + big.digits.length] += carry;
        }

        boolean resultIsNegative = this.isNegative != big.isNegative;
        return new BigInteger(normalize(resultDigits), resultIsNegative);
    }

    // 배열 앞쪽의 불필요한 0을 제거하는 헬퍼 함수
    private static int[] normalize(int[] digits) {
        int len = digits.length;
        while (len > 1 && digits[len - 1] == 0) {
            len--;
        }

        if (len == digits.length) {
            return digits;
        }
        return Arrays.copyOf(digits, len);
    }

    @Override
    public String toString() {
        if (digits.length == 1 && digits[0] == 0) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        if (isNegative) {
            sb.append("-");
        }
        for (int i = digits.length - 1; i >= 0; i--) {
            sb.append(digits[i]);
        }
        return sb.toString();
    }


    static BigInteger evaluate(String input) throws IllegalArgumentException {
        Matcher matcher = EXPRESSION_PATTERN.matcher(input);

        if (matcher.find()) {
            String sign1 = matcher.group(1);
            String numStr1 = matcher.group(2);
            String op = matcher.group(3);
            String sign2 = matcher.group(4);
            String numStr2 = matcher.group(5);

            BigInteger num1 = new BigInteger(sign1 + numStr1);
            BigInteger num2 = new BigInteger(sign2 + numStr2);

            switch (op) {
                case "+":
                    return num1.add(num2);
                case "-":
                    return num1.subtract(num2);
                case "*":
                    return num1.multiply(num2);
                default:
                    throw new IllegalArgumentException();
            }
        }
        throw new IllegalArgumentException(MSG_INVALID_INPUT);
    }

    public static void main(String[] args) throws Exception {
        try (InputStreamReader isr = new InputStreamReader(System.in)) {
            try (BufferedReader reader = new BufferedReader(isr)) {
                boolean done = false;
                while (!done) {
                    String input = reader.readLine();

                    try {
                        done = processInput(input);
                    } catch (IllegalArgumentException e) {
                        System.err.println(MSG_INVALID_INPUT);
                    }
                }
            }
        }
    }

    static boolean processInput(String input) throws IllegalArgumentException {
        boolean quit = isQuitCmd(input);

        if (quit) {
            return true;
        } else {
            BigInteger result = evaluate(input);
            System.out.println(result.toString());
            return false;
        }
    }

    static boolean isQuitCmd(String input) {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }
}