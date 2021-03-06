package net.jsmith.java.byteforge.decompiler.procyon;

import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.languages.java.JavaLanguage;

import net.jsmith.java.byteforge.decompiler.Decompiler;
import net.jsmith.java.byteforge.workspace.Container;
import net.jsmith.java.byteforge.workspace.Type;

public class ProcyonDecompiler implements Decompiler {
	
	private static final Logger LOG = LoggerFactory.getLogger( ProcyonDecompiler.class );
	
	private final Map< Container, ContainerTypeLoader > typeLoaders = new WeakHashMap< >( );
	
	@Override
	public String getName( ) {
		return "Procyon";
	}

	@Override
	public String decompile( Type type ) {
		if( LOG.isInfoEnabled( ) ) {
			LOG.info( "Decompiling type '{}' with procyon decompiler.", type.getMetadata( ).getFullName( ) );
		}
		ContainerTypeLoader loader;
		synchronized( this.typeLoaders ) {
			loader = this.typeLoaders.get( type.getContainer( ) );
			if( loader == null ) {
				loader = new ContainerTypeLoader( type.getContainer( ) );
				this.typeLoaders.put( type.getContainer( ), loader );
			}
		}
		MetadataSystem metadataSystem = loader.getMetadataSystem( );
		TypeDefinition def = metadataSystem.lookupType( type.getMetadata( ).getFullName( ) ).resolve( );
	
		DecompilationOptions options = new DecompilationOptions( );
		options.setFullDecompilation( true );
		options.getSettings( ).setForceExplicitImports( true );
		
		JavaHtmlOutput output = new JavaHtmlOutput( );
		new JavaLanguage( ).decompileType( def, output, options );
		
		return output.getHtml( );
	}

}
