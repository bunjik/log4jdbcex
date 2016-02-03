package info.bunji.jdbc.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

public class ClassScanUtilTest {

	@Test
	public void testFindClassesFromPackageInPath() throws Exception {
		List<Class<?>> classes = ClassScanUtil.findClassesFromPackage("info.bunji.jdbc.logger");

		assertThat(classes.contains(JdbcLoggerFactory.class), is(true));
		assertThat(classes.contains(JdbcLogger.class), is(true));
	}

	@Test
	public void testFindClassesFromPackageInJar() throws Exception {
		List<Class<?>> classes = ClassScanUtil.findClassesFromPackage("org.slf4j");

		assertThat(classes.contains(ILoggerFactory.class), is(true));
		assertThat(classes.contains(IMarkerFactory.class), is(true));
		assertThat(classes.contains(Logger.class), is(true));
		assertThat(classes.contains(LoggerFactory.class), is(true));
		assertThat(classes.contains(Marker.class), is(true));
		assertThat(classes.contains(MarkerFactory.class), is(true));
		assertThat(classes.contains(MDC.class), is(true));
	}

}
