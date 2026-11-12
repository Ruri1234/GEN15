<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }

    if (!function_exists('validPasswordInput')) {
        function validPasswordInput($data, $panjang = 50){
            if(strlen($data) > $panjang){
                return "";
            }
            return str_replace(array("'", "\"", "\\", "\0", "\r", "\n", "`", ";"), "", $data);
        }
    }

    if (!function_exists('validPasswordComplexity')) {
        function validPasswordComplexity($password){
            if(strlen($password) < 8 || strlen($password) > 50) return false;
            if(!preg_match('/[a-z]/', $password)) return false;
            if(!preg_match('/[A-Z]/', $password)) return false;
            if(!preg_match('/[0-9]/', $password)) return false;
            if(!preg_match('/[\W_]/', $password)) return false;
            return true;
        }
    }
?>
<div class="block-header">
    <h2><center>Ubah Password</center></h2>
</div>
<div class="row clearfix">
    <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
        <div class="card">
            <div class="body">
                <form id="form_validation" action="" method="POST">
                    <label for="passwordlama">Password Lama</label>
                    <div class="form-group">
                        <div class="form-line">
                            <input name="passwordlama" type="password" id="passwordlama" class="form-control" placeholder="Masukkan password lama..." maxlength="50" autocomplete="off" autofocus required/>
                        </div>
                    </div>
                    <label for="passwordbaru">Password Baru</label>
                    <div class="form-group">
                        <div class="form-line">
                            <input name="passwordbaru" type="password" id="passwordbaru" class="form-control" placeholder="Masukkan password baru..." pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_]).{8,50}" title="Minimal 8 karakter, memuat kombinasi huruf besar (A-Z), huruf kecil (a-z), angka (0-9), dan simbol khusus (*,#,$,!,&, dll)" maxlength="50" autocomplete="off" required/>
                        </div>
                        <small class="text-muted" style="display:block; margin-top:5px; color:#555; font-size:12px;">
                            <strong>Kebijakan Keamanan Password:</strong> Panjang minimal 8 karakter, harus memuat kombinasi huruf besar (A-Z), huruf kecil (a-z), angka (0-9), dan karakter khusus/simbol (*, #, $, !, &, dll).
                        </small>
                    </div>
                    <label for="passwordbaruulangi">Ulangi Password Baru</label>
                    <div class="form-group">
                        <div class="form-line">
                            <input name="passwordbaruulangi" type="password" id="passwordbaruulangi" class="form-control" placeholder="Masukkan password baru sekali lagi..." pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_]).{8,50}" title="Minimal 8 karakter, memuat kombinasi huruf besar (A-Z), huruf kecil (a-z), angka (0-9), dan simbol khusus (*,#,$,!,&, dll)" maxlength="50" autocomplete="off" required/>
                        </div>
                    </div>
                    <center><button class="btn btn-danger waves-effect" type="submit" name="btnSimpan">Simpan</button></center>
                </form>
                <?php 
                    $btnSimpan=isset($_POST['btnSimpan'])?$_POST['btnSimpan']:NULL;
                    if (isset($btnSimpan)) {
                         $passwordlama        = validPasswordInput($_POST['passwordlama'],50);
                         $passwordbaru        = validPasswordInput($_POST['passwordbaru'],50);
                         $passwordbaruulangi  = validPasswordInput($_POST['passwordbaruulangi'],50);
                         if(getOne2("select AES_DECRYPT(personal_pasien.password,'windi') from personal_pasien where personal_pasien.no_rkm_medis='".cleankar(encrypt_decrypt($_SESSION["ses_pasien"],"d"))."'")==$passwordlama){
                             if($passwordbaru==$passwordbaruulangi){
                                 // Klausul 1.7: Validasi Kompleksitas Password Minimal
                                 if(validPasswordComplexity($passwordbaru)){
                                     if(bukaquery2("update personal_pasien set password=AES_ENCRYPT('$passwordbaru','windi') where personal_pasien.no_rkm_medis='".cleankar(encrypt_decrypt($_SESSION["ses_pasien"],"d"))."'")){
                                         echo "<div class='row clearfix'>
                                                    <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                                                        <div class='card'>
                                                            <div class='body bg-danger'>
                                                                <center><h4>Ubah password berhasil...</h4></center>
                                                            </div>
                                                        </div>
                                                    </div>
                                               </div>";
                                     }else{
                                         echo "<div class='row clearfix'>
                                                    <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                                                        <div class='card'>
                                                            <div class='body bg-success'>
                                                                <center><h4>Gagal ubah password. Silahkan ulangi...!</h4></center>
                                                            </div>
                                                        </div>
                                                    </div>
                                               </div>";
                                     }
                                 }else{
                                     echo "<div class='row clearfix'>
                                                <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                                                    <div class='card'>
                                                        <div class='body bg-success'>
                                                            <center><h4>Gagal mengubah password.<br/>Password tidak memenuhi kriteria kompleksitas!<br/><small style='color:#fff;'>Password minimal 8 karakter, serta memuat kombinasi huruf besar (A-Z), huruf kecil (a-z), angka (0-9), dan karakter khusus (*,#,$,!,&, dll).</small></h4></center>
                                                        </div>
                                                    </div>
                                                </div>
                                           </div>";
                                 }
                             }else{
                                 echo "<div class='row clearfix'>
                                            <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                                                <div class='card'>
                                                    <div class='body bg-success'>
                                                        <center><h4>Gagal mengubah password.<br/>Password baru dan konfirmasi tidak cocok. Silahkan ulangi...!</h4></center>
                                                    </div>
                                                </div>
                                            </div>
                                       </div>";
                             }
                         }else{
                             echo "<div class='row clearfix'>
                                        <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                                            <div class='card'>
                                                <div class='body bg-success'>
                                                    <center><h4>Gagal mengubah password.<br/>Password lama salah. Silahkan ulangi...!</h4></center>
                                                </div>
                                            </div>
                                        </div>
                                   </div>";
                         }
                    }
                ?>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js" type="text/javascript"></script>
