
 // 
 // 	Coded By: 	Josh Woolbright
 // 		Date: 	10/11/2019
 //
 //  Description:	This code contains functions to add and multiply 32 bit floating point
 //					numbers. See FPNumber.java for a more detailed description of the 
 //					floating point number architecture.
 //

public class fp 
{
	
	public int add(int a, int b)
	{
		FPNumber fa = new FPNumber(a);
		FPNumber fb = new FPNumber(b);
		FPNumber result = new FPNumber(0);

		// Handle Exception cases
		if (fa.isNaN())
			return a;
		if (fb.isNaN())
			return b;
		if (fa.isZero())
			return b;
		if (fb.isZero())
			return a;
		if (fa.isInfinity() && fb.isInfinity())
		{
			if (fa.s() == fb.s())
				return a;
			else
				throw new IllegalStateException(); // Indicates that the result is not a number
		}
		if (fa.isInfinity())
			return a;
		if (fb.isInfinity())
			return b;
		
		// Temporary variables used to keep track of exponents and mantissas
		// Af is > Bf, and Rf & Re pertain to the result
		int shift;
		long Af;
		long Bf;
		long Rf;
		int Re;
		
		// Determines if |fa| or |fb| is larger
		if ((Math.abs(Float.intBitsToFloat(fa.asInt()))) >= (Math.abs(Float.intBitsToFloat(fb.asInt()))))
		{				
			shift = fa.e() - fb.e();
			if (shift > 24)
				return fa.asInt();
			Re = fa.e();
			Af = fa.f();
			Bf = fb.f() >> shift;
			result.setS(fa.s());
			
		}
		else
		{
			shift = fb.e() - fa.e();
			if (shift > 24)
				return fb.asInt();
			Re = fb.e();
			Af = fb.f();
			Bf = fa.f() >> shift;
			result.setS(fb.s());
		}
		
		// Adds or subtract based off the signs
		if (fa.s() == fb.s())
		{
			Rf = Af + Bf;
		}
		else
		{
			Rf = Af - Bf;
			if (Rf == 0)
			{
				result.setE(0);
				result.setF(0);
				return result.asInt();
			}
		}
	
		// If mantissa is greater than 27 bits, then shift right
		while (Rf >= 0x4000000) 
		{
			Re++;
			if (Re > 254) // Exponent overflow
			{
				if (result.s() == 1)
					return Integer.MAX_VALUE; // Returns positive infinity
				else
					return Integer.MIN_VALUE; // Returns negative infinity
			}
			Rf = Rf >> 1;
		}
		// If less than 26 bits, then shift left
		while (Rf < 0x2000000 && Re > 0)
		{
			Re--;
			Rf = Rf << 1;	
		}
		
		result.setE(Re);
		result.setF(Rf);
		
		return result.asInt();
	}

	public int mul(int a, int b)
	{
		FPNumber fa = new FPNumber(a);
		FPNumber fb = new FPNumber(b);
		FPNumber result = new FPNumber(0);

		// Handle exception cases
		if (fa.isNaN())
			return a;
		if (fb.isNaN())
			return b;
		if (fa.isZero() && fb.isInfinity())
			throw new IllegalStateException();
		if (fa.isInfinity() && fa.isZero())
			throw new IllegalStateException();
		if (fa.isZero() || fb.isZero())
		{
			if (fa.s() == fb.s())
			{
				result.setS(1);
				result.setE(0);
				result.setF(0);
				return result.asInt();
			}
			else
			{
				result.setS(-1);
				result.setE(0);
				result.setF(0);
				return result.asInt();
			}
		}
		if (fa.isInfinity() || fb.isInfinity())
		{
			if (fa.s() == fb.s())
				return Integer.MAX_VALUE;
			else
				return Integer.MIN_VALUE;
		}

		// Determine sign
		if ((fa.s() == 1 && fb.s() == 1) || (fa.s() == -1 && fb.s() == -1))
			result.setS(1);
		else
			result.setS(-1);
		
		result.setE((fa.e() + fb.e() - 127));
		
		// Handles exponent overflow and underflow
		if (result.e() > 254)
		{
			if(result.s() == 1)
				return Integer.MAX_VALUE;
			else
				return Integer.MIN_VALUE;
		}
		if (result.e() < 0)
		{
			result.setE(0);
			result.setF(0);
			return result.asInt();
		}
		
		result.setF((fa.f() * fb.f()) >> 26);
		result.setE(result.e() + 1);
		
		// If mantissa is less than 26 bits, shift left
		if (result.f() < 0x2000000)
		{
			result.setE(result.e() - 1);
			result.setF(result.f() << 1);
		}
		return result.asInt();
	}
	
	// Test Code
	public static void main(String[] args)
	{
		int v24_25	= 0x41C20000; // 24.25
		int v_1875	= 0xBE400000; // -0.1875
		int v5		= 0xC0A00000; // -5.0

		fp m = new fp();	
		
		System.out.println(Float.intBitsToFloat(m.add(v24_25, v_1875)) + " should be 24.0625");
		System.out.println(Float.intBitsToFloat(m.add(v24_25, v5)) + " should be 19.25");
		System.out.println(Float.intBitsToFloat(m.add(v_1875, v5)) + " should be -5.1875");
		
		System.out.println(Float.intBitsToFloat(m.mul(v24_25, v_1875)) + " should be -4.546875");
		System.out.println(Float.intBitsToFloat(m.mul(v24_25, v5)) + " should be -121.25");
		System.out.println(Float.intBitsToFloat(m.mul(v_1875, v5)) + " should be 0.9375");

	}
}

