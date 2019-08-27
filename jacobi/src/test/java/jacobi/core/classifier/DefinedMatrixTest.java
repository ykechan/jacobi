package jacobi.core.classifier;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.DefinedMatrix;
import jacobi.core.classifier.DefinedMatrix.Nominal;

public class DefinedMatrixTest {
	
	@Test
	public void shouldBeAbleToViewNominalColumnsOnMatrix() {
		Matrix data = Matrices.wrap(new double[][] {
			new double[] { 
				Occupation.BARD.ordinal(), Boolean.FALSE ? 1 : 0, 63.5
			},
			new double[] { 
				Occupation.ROGUE.ordinal(), Boolean.TRUE ? 1 : 0, 72.8
			},
			new double[] { 
				Occupation.WIZARD.ordinal(), Boolean.FALSE ? 1 : 0, 55.9
			},
			new double[] { 
				Occupation.WARRIOR.ordinal(), Boolean.TRUE ? 1 : 0, 103.1
			}
		});
		
		DefinedMatrix<String> defMat = DefinedMatrix.of(data, Arrays.asList("win", "win", "lose", "win"))
			.apply(Arrays.asList(
				Column.of(0, Occupation.class),
				Column.of(1, Boolean.class)
			));
		
		Assert.assertArrayEquals(new Object[] {
			new Instance(3, 1, 1.0),
			new Instance(2, 1, 1.0),
			new Instance(1, 0, 1.0),
			new Instance(0, 1, 1.0)
		}, 
		defMat.getInstances(Column.of(0, Occupation.class)).toArray());
		
		Assert.assertArrayEquals(new Object[] {
			new Instance(0, 1, 1.0),
			new Instance(1, 1, 1.0),
			new Instance(0, 0, 1.0),
			new Instance(1, 1, 1.0)
		}, 
		defMat.getInstances(Column.of(1, Boolean.class)).toArray());
	}
	
	@Test
	public void shouldBeAbleToViewNumericColumnsOnMatrix() {
		Matrix data = Matrices.wrap(new double[][] {
			new double[] { 
				Occupation.BARD.ordinal(), Boolean.FALSE ? 1 : 0, 63.5
			},
			new double[] { 
				Occupation.ROGUE.ordinal(), Boolean.TRUE ? 1 : 0, 72.8
			},
			new double[] { 
				Occupation.WIZARD.ordinal(), Boolean.FALSE ? 1 : 0, 55.9
			},
			new double[] { 
				Occupation.WARRIOR.ordinal(), Boolean.TRUE ? 1 : 0, 103.1
			}
		});
		
		DefinedMatrix<String> defMat = DefinedMatrix.of(data, Arrays.asList("win", "win", "lose", "win"))
			.apply(Arrays.asList(
				Column.of(0, Occupation.class),
				Column.of(1, Boolean.class),
				Column.numeric(2)
			));
		
		Assert.assertArrayEquals(new Object[] {
			new Instance(0, 1, 1.0),
			new Instance(1, 1, 1.0),
			new Instance(2, 0, 1.0),
			new Instance(3, 1, 1.0)
		}, 
		defMat.getInstances(Column.numeric(2)).toArray());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenViewNominalAsNumeric() {
		Matrix data = Matrices.wrap(new double[][] {
			new double[] { 
				Occupation.BARD.ordinal(), Boolean.FALSE ? 1 : 0, 63.5
			},
			new double[] { 
				Occupation.ROGUE.ordinal(), Boolean.TRUE ? 1 : 0, 72.8
			},
			new double[] { 
				Occupation.WIZARD.ordinal(), Boolean.FALSE ? 1 : 0, 55.9
			},
			new double[] { 
				Occupation.WARRIOR.ordinal(), Boolean.TRUE ? 1 : 0, 103.1
			}
		});
		
		DefinedMatrix.of(data, Arrays.asList("win", "win", "lose", "win"))
			.apply(Arrays.asList(
				Column.of(0, Occupation.class),
				Column.of(1, Boolean.class),
				Column.numeric(2)
			)).getInstances(Column.numeric(1));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenViewNumericAsNominal() {
		Matrix data = Matrices.wrap(new double[][] {
			new double[] { 
				Occupation.BARD.ordinal(), Boolean.FALSE ? 1 : 0, 63.5
			},
			new double[] { 
				Occupation.ROGUE.ordinal(), Boolean.TRUE ? 1 : 0, 72.8
			},
			new double[] { 
				Occupation.WIZARD.ordinal(), Boolean.FALSE ? 1 : 0, 55.9
			},
			new double[] { 
				Occupation.WARRIOR.ordinal(), Boolean.TRUE ? 1 : 0, 103.1
			}
		});
		
		DefinedMatrix.of(data, Arrays.asList("win", "win", "lose", "win"))
			.apply(Arrays.asList(
				Column.of(0, Occupation.class),
				Column.of(1, Boolean.class),
				Column.numeric(2)
			)).getInstances(Column.signed(2));
	}
	
	@Test
	public void shouldBeAbleToExtractBooleanFromMatrix() {
		Nominal<Boolean> nom = DefinedMatrix.extractNominals(Matrices.wrap(new double[][] {
			{Math.PI,  1.0, Math.E},
			{Math.PI, -1.0, Math.E},
			{Math.PI,  1.0, Math.E},
			{Math.PI, -1.0, Math.E},
			{Math.PI,  1.0, Math.E},
			{Math.PI, -1.0, Math.E}
		}), Column.signed(1));
		
		Assert.assertEquals(Column.signed(1), nom.def);
		Assert.assertArrayEquals(new int[] {1, 0, 1, 0, 1, 0}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToExtractEnumsFromMatrix() {
		Nominal<Occupation> nom = DefinedMatrix.extractNominals(Matrices.wrap(new double[][] {
			{Math.PI, 0.0, Math.E},
			{Math.PI, 1.0, Math.E},
			{Math.PI, 2.0, Math.E},
			{Math.PI, 0.0, Math.E},
			{Math.PI, 1.0, Math.E},
			{Math.PI, 2.0, Math.E}
		}), Column.of(1, Occupation.class));
		
		Assert.assertEquals(Column.of(1, Occupation.class), nom.def);
		Assert.assertArrayEquals(new int[] {0, 1, 2, 0, 1, 2}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToDetectAndExtractBooleanItems() {
		Nominal<Boolean> nom = DefinedMatrix.extractNominals(10, Arrays.asList(
			Boolean.FALSE,
			Boolean.TRUE,
			Boolean.TRUE,
			Boolean.TRUE,
			Boolean.FALSE
		));
		
		Assert.assertEquals(10, nom.def.getIndex());
		Assert.assertEquals(2, nom.def.cardinality());
		Assert.assertEquals(Boolean.FALSE, nom.def.valueOf(0));
		Assert.assertEquals(Boolean.TRUE, nom.def.valueOf(1));
		
		Assert.assertArrayEquals(new int[] {0, 1, 1, 1, 0}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToDetectAndExtractBooleanItemsWithAllFalseOrAllTrue() {
		Nominal<Boolean> nom = DefinedMatrix.extractNominals(10, Arrays.asList(
			Boolean.FALSE,
			Boolean.FALSE
		));
			
		Assert.assertEquals(10, nom.def.getIndex());
		Assert.assertEquals(2, nom.def.cardinality());
		Assert.assertEquals(Boolean.FALSE, nom.def.valueOf(0));
		Assert.assertEquals(Boolean.TRUE, nom.def.valueOf(1));
			
		Assert.assertArrayEquals(new int[] {0, 0}, nom.values);
		
		nom = DefinedMatrix.extractNominals(10, Arrays.asList(
			Boolean.TRUE,
			Boolean.TRUE,
			Boolean.TRUE,
			Boolean.TRUE
		));
				
		Assert.assertEquals(10, nom.def.getIndex());
		Assert.assertEquals(2, nom.def.cardinality());
		Assert.assertEquals(Boolean.FALSE, nom.def.valueOf(0));
		Assert.assertEquals(Boolean.TRUE, nom.def.valueOf(1));
			
		Assert.assertArrayEquals(new int[] {1, 1, 1, 1}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToDetectAndExtractEnums() {
		Nominal<Occupation> nom = DefinedMatrix.extractNominals(7, Arrays.asList(
			Occupation.WARRIOR,
			Occupation.WIZARD,
			Occupation.ROGUE,
			Occupation.BARD
		));
		
		Assert.assertEquals(7, nom.def.getIndex());
		Assert.assertEquals(4, nom.def.cardinality());
		Assert.assertEquals(Occupation.WARRIOR, nom.def.valueOf(0));
		Assert.assertEquals(Occupation.WIZARD, nom.def.valueOf(1));
		Assert.assertEquals(Occupation.ROGUE, nom.def.valueOf(2));
		Assert.assertEquals(Occupation.BARD, nom.def.valueOf(3));
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToDetectAndExtractEnumsWithPureValue() {
		Nominal<Occupation> nom = DefinedMatrix.extractNominals(7, Arrays.asList(
			Occupation.WIZARD,
			Occupation.WIZARD,
			Occupation.WIZARD
		));
		
		Assert.assertEquals(7, nom.def.getIndex());
		Assert.assertEquals(4, nom.def.cardinality());
		Assert.assertEquals(Occupation.WARRIOR, nom.def.valueOf(0));
		Assert.assertEquals(Occupation.WIZARD, nom.def.valueOf(1));
		Assert.assertEquals(Occupation.ROGUE, nom.def.valueOf(2));
		Assert.assertEquals(Occupation.BARD, nom.def.valueOf(3));
		Assert.assertArrayEquals(new int[] {1, 1, 1}, nom.values);
	}
	
	@Test
	public void shouldBeAbleToDetectAndExtractCustomLabels() {
		Nominal<String> nom = DefinedMatrix.extractNominals(7, Arrays.asList(
			"Paladin",
			"Necromancer",
			"Barbarian",
			"Necromancer",
			"Paladin",
			"Paladin",
			"Barbarian"
		));
		
		String[] labels = {"Paladin", "Necromancer", "Barbarian"};
		Arrays.sort(labels);
		
		Assert.assertEquals(7, nom.def.getIndex());
		Assert.assertEquals(3, nom.def.cardinality());
		Assert.assertEquals(labels[0], nom.def.valueOf(0));
		Assert.assertEquals(labels[1], nom.def.valueOf(1));
		Assert.assertEquals(labels[2], nom.def.valueOf(2));
		
		Assert.assertArrayEquals(new int[] {2, 1, 0, 1, 2, 2, 0}, nom.values);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenGivenNumericItems() {
		DefinedMatrix.extractNominals(7, Arrays.asList(
			Math.PI, Math.E
		));
	}
	
	public enum Occupation {
		WARRIOR, WIZARD, ROGUE, BARD
	}

}
