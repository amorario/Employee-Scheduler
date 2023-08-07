package com.luv2code.springboot.thymeleafdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

//@SpringBootApplication
public class ThymeleafdemoApplication {

	public static int randomGen () {
		Random r = new Random();
		double[] p = new double[]{0.2,0.4,0.15,0.25};

		float myVal = r.nextFloat();
		float probSum = 0.0F;

		for (int i = 0; i < p.length; i++) {
			probSum += p[i];
			if (myVal <= probSum) {
				return i;
			}
		}
		return p.length-1;
	}



	public static void main(String[] args) {
		//SpringApplication.run(ThymeleafdemoApplication.class, args);

		/* WEIGHTED DICE ROLL on '6' rolls
		int min = 1;
		int max = 50;
		int rolls = 20;
		double percentage = 0;
		while (rolls >0) {
			int randomValue = min + (int) (Math.random() * ((max - min) + 1));
			int diceValue = (randomValue < 6) ? randomValue : 6;
			System.out.println("randomValue = "+ randomValue+ ", diceValue = "+diceValue + " ");
			if (diceValue == 6)
				percentage+=1;
			rolls--;
			System.out.println("\n6 roll % : " + ((percentage/20)*100) + "%");
		}*/

		/* randomGen()
		int count = 100;
		double ones= 0;
		double twos = 0;
		double zeroes= 0;
		double threes = 0;

		while (count > 0) {
			int n = randomGen();
			if (n == 0)
				zeroes++;
			if (n == 1)
				ones++;
			if (n == 2)
				twos++;
			if (n == 3)
				threes++;
			//System.out.println(n);
			count--;
		}
		System.out.println("zeroes: " + ((zeroes/100)*100) + "%");
		System.out.println("ones: " + ((ones/100)*100) + "%");
		System.out.println("twos: " + ((twos/100)*100) + "%");
		System.out.println("threes: " + ((threes/100)*100) + "%");
		*/

		int count = 10;
		while (count > 0) {
			if (Math.random() < .5) {
				System.out.println("Heads");
			} else {
				System.out.println("Tails");
			}
			count--;
		}

	}

}
