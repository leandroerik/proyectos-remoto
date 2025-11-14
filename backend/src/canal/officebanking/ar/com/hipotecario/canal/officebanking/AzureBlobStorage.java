package ar.com.hipotecario.canal.officebanking;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;

public class AzureBlobStorage extends ModuloOB {

    public static BlobServiceClient GetBlobServiceClient() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // Azure SDK client builders accept the credential as a parameter
        // TODO: Replace <storage-account-name> with your actual storage account name

        return new BlobServiceClientBuilder()
                .endpoint("https://stbancaautomaticadesa.blob.core.windows.net/officebanking/")
                .credential(credential)
                .buildClient();
    }

    public static void listContainersWithPaging(BlobServiceClient blobServiceClient) {
        // Set a prefix to filter results and specify a page limit
        ListBlobContainersOptions options = new ListBlobContainersOptions()
                .setMaxResultsPerPage(2)  // Low number for demonstration purposes
                .setPrefix("container-");

        int i = 0;
        Iterable<PagedResponse<BlobContainerItem>> blobContainerPages = blobServiceClient
                .listBlobContainers(options, null).iterableByPage();
        for (PagedResponse<BlobContainerItem> page : blobContainerPages) {
            System.out.printf("Page %d%n", ++i);
            page.getElements().forEach(container -> {
                System.out.printf("Name: %s%n", container.getName());
            });
        }
    }


    public static void listBlobsHierarchicalListing(BlobContainerClient blobContainerClient, String prefix/* ="" */) {
        String delimiter = "/";
        ListBlobsOptions options = new ListBlobsOptions()
                .setPrefix(prefix);


        blobContainerClient.listBlobsByHierarchy(delimiter, options, null)
                .forEach(blob -> {
                    if (blob.isPrefix()) {
                        System.out.printf("Virtual directory prefix: %s%n", delimiter + blob.getName());
                        listBlobsHierarchicalListing(blobContainerClient, blob.getName());
                    } else {
                        System.out.printf("Blob name: %s%n", blob.getName());
                    }
                });
    }

}
