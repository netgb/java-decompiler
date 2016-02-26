package net.jsmith.java.decomp.asm;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jsmith.java.decomp.container.TypeMetadata;

public class TypeMetadataLoader {

	private static final Logger LOG = LoggerFactory.getLogger( TypeMetadataLoader.class );
	
	public static TypeMetadata loadMetadataFromStream( InputStream is ) throws IOException {
		MetadataVisitor mv = new MetadataVisitor( );
		new ClassReader( is ).accept( mv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES );
		
		return mv.getTypeMetadata( );
	}
	
	private static class MetadataVisitor extends ClassVisitor implements Opcodes {
		
		private final TypeMetadata.Builder builder;
		
		public MetadataVisitor( ) {
			this( null );
		}
		
		public MetadataVisitor( ClassVisitor delegate ) {
			super( ASM5, delegate );
			
			this.builder = TypeMetadata.builder( );
		}

		@Override
		public void visit( int version, int access, String name, String signature, String superName, String[ ] interfaces ) {
			super.visit( version, access, name, signature, superName, interfaces );
			
			if( LOG.isDebugEnabled( ) ) {
				LOG.debug( "Parsing metadata for class '{}' with access flags '{}'.", name, access );
			}
			this.builder.setTypeName( toDottedName( name ) );
			this.builder.setFlags( access );
		}

		@Override
		public void visitOuterClass( String owner, String name, String desc ) {
			super.visitOuterClass( owner, name, desc );
			
			if( LOG.isDebugEnabled( ) ) {
				LOG.debug( "Found outer class information '{}.{}.{}'", owner, name, desc );
			}
			this.builder.setEnclosingType( toDottedName( owner ) );
			this.builder.setEnclosingMethod( toDottedName( name ) );
			this.builder.setEnclosingMethodDesc( desc );
		}

		@Override
		public void visitInnerClass( String name, String outerName, String innerName, int access ) {
			super.visitInnerClass( name, outerName, innerName, access );
			
			if( LOG.isDebugEnabled( ) ) {
				LOG.debug( "Found inner class reference '{}'.", name );
			}
			this.builder.addEnclosedType( toDottedName( name ) );
		}

		public TypeMetadata getTypeMetadata( ) {
			return this.builder.build( );
		}
		
		private String toDottedName( String typeName ) {
			if( typeName == null ) {
				return null;
			}
			return typeName.replace( '/', '.' );
		}
		
	}
	
}