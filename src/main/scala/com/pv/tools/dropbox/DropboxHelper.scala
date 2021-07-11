/*
 *  Created on: July 6, 2021
 *      Author: Srinivasan PS
 */
package com.pv.tools.dropbox

object DropboxHelper {

}
//
// package com.pv.util
//
//class DropboxHelper {
//  private:
//  string dropbox_config_file;
//  string dropbox_access_token;
//
//  CryptoHelper *crypto_helper;
//
//  public:
//
//    DropboxHelper(string config_file, CryptoHelper* p) {
//    dropbox_config_file = config_file;
//
//    crypto_helper = p;
//
//    if(is_dropbox_setup()) {
//      get_dropbox_access_token();
//    } else {
//      dropbox_access_token = "";
//    }
//  }
//
//  string get_dropbox_access_token() {
//    if(dropbox_access_token == "") {
//      if(!is_dropbox_setup()) {
//        throw "Dropbox not setup.";
//      }
//      ifstream ifs(dropbox_config_file);
//      string encrypted_token(
//        (istreambuf_iterator<char>(ifs)),
//        (istreambuf_iterator<char>()));
//
//      dropbox_access_token = crypto_helper->decrypt(encrypted_token);
//    }
//
//    return dropbox_access_token;
//  }
//
//  void set_dropbox_access_token(string token) {
//    remove(dropbox_config_file.c_str());
//
//    ofstream fout();
//    fstream ofs(dropbox_config_file, ios::app);
//    ofs<<crypto_helper->encrypt(token);
//    ofs.close();
//
//    dropbox_access_token = token;
//  }
//
//  bool is_dropbox_setup() {
//    ifstream f(dropbox_config_file);
//    return f.good();
//  }
//
//  void make_backup_in_dropbox(string file) {
//    string backup_file = file + "." + to_string(unix_timestamp()) + ".backup";
//    download_file(file, backup_file);
//    upload_file(backup_file);
//    remove(backup_file.c_str());
//  }
//
//  void download_file(string vault_file, string temp_download_file) {
//    if(!is_dropbox_setup()) {
//      cout<<"ERROR: Setup dropbox account first.\n";
//      return;
//    }
//    string dropbox_file = "/srini_vault/" + vault_file + ".dropbox";
//    remove(temp_download_file.c_str());
//
//    CURL *curl;
//    FILE *fp;
//    CURLcode res;
//    curl_global_init(CURL_GLOBAL_ALL);
//    curl = curl_easy_init();
//    if(curl) {
//      fp = fopen(temp_download_file.c_str(),"wb");
//
//      struct curl_slist *headers=NULL; /* init to NULL is important */
//      headers = curl_slist_append(headers, ("Authorization: Bearer " + get_dropbox_access_token()).c_str());
//      headers = curl_slist_append(headers, ("Dropbox-API-Arg: {\"path\" : \"/srini_vault/my_vault.json.dropbox\"}"));
//
//      curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
//      curl_easy_setopt(curl, CURLOPT_URL, "https://content.dropboxapi.com/2/files/download");
//      curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION,write_data) ;
//      curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
//
//      res = curl_easy_perform(curl);
//      if(res != CURLE_OK)
//        fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
//
//      curl_easy_cleanup(curl);
//      fclose(fp);
//    }
//    curl_global_cleanup();
//    printf ("Done Download!\n");
//  }
//
//  void delete_file(string dropbox_file) {
//    if(!is_dropbox_setup()) {
//      cout<<"ERROR: Setup dropbox account first.\n"<<("{\"path\":\"/srini_vault/" + dropbox_file + "\"}").c_str()<<endl;
//      return;
//    }
//
//    CURL *curl;
//    CURLcode res;
//    curl_global_init(CURL_GLOBAL_ALL);
//    curl = curl_easy_init();
//    if(curl)
//    {
//      struct curl_slist *headers=NULL; /* init to NULL is important */
//      headers = curl_slist_append(headers, ("Authorization: Bearer " + get_dropbox_access_token()).c_str());
//      headers = curl_slist_append(headers, "Content-Type: application/json");
//
//      curl_easy_setopt(curl, CURLOPT_URL, "https://api.dropboxapi.com/2/files/delete_v2");
//      curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
//      curl_easy_setopt(curl, CURLOPT_POSTFIELDS, "{\"path\" : \"/srini_vault/my_vault.json.dropbox\"}");// ("{\"path\":\"/srini_vault/" + dropbox_file + "\"}").c_str());
//
//      res = curl_easy_perform(curl);
//      if(res != CURLE_OK)
//        fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
//
//      curl_easy_cleanup(curl);
//    }
//    curl_global_cleanup();
//    printf ("Done Deletion!\n");
//  }
//
//
//
//  void upload_file(string vault_file) {
//    if(!is_dropbox_setup()) {
//      cout<<"ERROR: Setup dropbox account first.\n";
//      return;
//    }
//
//    string dropbox_file = "/srini_vault/" + vault_file + ".dropbox";
//
//    delete_file(dropbox_file);
//
//    CURL *curl;
//    FILE *fp;
//    CURLcode res;
//    curl_global_init(CURL_GLOBAL_ALL);
//    curl = curl_easy_init();
//    if(curl)
//    {
//      fp = fopen(vault_file.c_str(),"rb");
//      struct stat st;
//      stat(vault_file.c_str(), &st);
//
//      char *post_params;
//
//      struct curl_slist *headers=NULL; /* init to NULL is important */
//      headers = curl_slist_append(headers, ("Authorization: Bearer " + get_dropbox_access_token()).c_str());
//      headers = curl_slist_append(headers, "Content-Type: application/octet-stream");
//      headers = curl_slist_append(headers, ("Dropbox-API-Arg: {\"path\":\"" + dropbox_file + "\",\"mode\": \"overwrite\",\"autorename\": true,\"mute\": false}").c_str());
//
//      curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
//      curl_easy_setopt(curl, CURLOPT_URL, "https://content.dropboxapi.com/2/files/upload");
//      curl_easy_setopt(curl, CURLOPT_POST, 1L);
//      curl_easy_setopt(curl, CURLOPT_POSTFIELDS, post_params);
//      curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE_LARGE, st.st_size);
//      curl_easy_setopt(curl, CURLOPT_READFUNCTION, read_callback);
//      curl_easy_setopt(curl, CURLOPT_READDATA, fp);
//
//      res = curl_easy_perform(curl);
//      if(res != CURLE_OK)
//        fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
//
//      long http_code;
//      cout<<curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &http_code)<<endl;
//      cout<<http_code<<endl;
//
//      curl_easy_cleanup(curl);
//      fclose(fp);
//
//    }
//    curl_global_cleanup();
//    printf ("Done!\n");
//  }
//  // ------------- USER FUNCTIONS --------------------
//};
