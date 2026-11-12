<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }

    // Deklarasi fungsi sanitasi mandiri agar tidak terjadi Fatal Error jika conf.php di hosting belum terupdate
    if (!function_exists('validPasswordInput')) {
        function validPasswordInput($data, $panjang = 50){
            if(strlen($data) > $panjang){
                return "";
            }
            return str_replace(array("'", "\"", "\\", "\0", "\r", "\n", "`", ";"), "", $data);
        }
    }
?>
<head>
   <link rel="stylesheet" type="text/css" href="capca.css">
</head>
<body>
     <section id="appointment" data-stellar-background-ratio="3">
          <div class="container">
               <div class="row">
                    <div class="col-md-6 col-sm-6">
                         <img src="images/appointment-image.jpg" class="img-responsive" alt="">
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <div class="section-title wow fadeInUp" data-wow-delay="0.4s">
                           <h2><center>Log In Pasien</center></h2>
                        </div>
                        <div class="wow fadeInUp" data-wow-delay="0.8s">
                            <div class="col-md-12 col-sm-12">
                                Jika anda pasien lama atau pernah berobat sebelumnya, untuk nomor rekam medis dan password login bisa Anda tanyakan kepada petugas Kami saat Anda melakukan registrasi secara offline. Dan password bisa Anda ubah setelah login di aplikasi EPasien. Jika Anda pasien baru dan belum pernah periksa sebelumnya, silahkan melakukan booking atau buat janji melalui menu utama EPasien ini. Setelah admin kami melakukan verifikasi data, Anda akan mendapat password login dan antrian periksa sesuai booking Anda.<br/><br/><br/>
                            </div>
                            <?php 
                                $lockout_duration = 300; // 5 menit lockout temporer (Klausul 1.4)
                                $max_attempts     = 3;   // Maksimal 3 kali salah percobaan login (Klausul 1.4)
                                $pesan_warning    = "";
                                $is_locked_out    = false;
                                $sisa_menit       = 0;

                                // Cek status Lockout Temporer (Klausul 1.4)
                                if (isset($_SESSION['lockout_until'])) {
                                    if (time() < $_SESSION['lockout_until']) {
                                        $is_locked_out = true;
                                        $sisa_detik    = $_SESSION['lockout_until'] - time();
                                        $sisa_menit    = ceil($sisa_detik / 60);
                                        // Klausul 1.5: Pesan Warning "Gagal Login"
                                        $pesan_warning = "Gagal Login! Akses login Anda terkunci sementara setelah 3 kali salah percobaan. Silakan tunggu sekitar $sisa_menit menit lagi sebelum mencoba kembali.";
                                    } else {
                                        // Masa lockout telah selesai, pulihkan counter
                                        unset($_SESSION['lockout_until']);
                                        $_SESSION['login_attempts'] = 0;
                                        $is_locked_out = false;
                                    }
                                }

                                $BtnLogin = isset($_POST['BtnLogin']) ? $_POST['BtnLogin'] : NULL;
                                if (isset($BtnLogin)) {
                                    if ($is_locked_out) {
                                        // Klausul 1.4 & 1.5: Tolak eksekusi jika masih dalam periode lockout
                                        $pesan_warning = "Gagal Login! Akses masih terkunci sementara. Silakan tunggu sekitar $sisa_menit menit lagi.";
                                    } else {
                                        // Klausul 1.3: Mekanisme pembatasan, Captcha Anti-Bypass & Anti-Reuse
                                        $inputcaptcha = isset($_POST["inputcaptcha"]) ? validangka(trim($_POST["inputcaptcha"])) : 0;
                                        $saved_captcha = isset($_SESSION["Capcay"]) ? $_SESSION["Capcay"] : "";

                                        // Anti-Reuse: Segera hapus CAPTCHA session agar tidak dapat di-reuse pada percobaan berikutnya
                                        unset($_SESSION["Capcay"]);

                                        // Validasi CAPTCHA (Anti-Bypass: jika kosong atau salah langsung ditolak sebelum query kredensial)
                                        if (empty($saved_captcha) || empty($inputcaptcha) || $saved_captcha != getOne2("select aes_encrypt(".$inputcaptcha.",'windi')")) {
                                            // Klausul 1.3: Delay time (0.5 detik) untuk memperlambat serangan brute force otomatis
                                            usleep(500000);

                                            $_SESSION['login_attempts'] = (isset($_SESSION['login_attempts']) ? (int)$_SESSION['login_attempts'] : 0) + 1;

                                            // Klausul 1.4: Lockout temporer setelah 3 kali salah
                                            if ($_SESSION['login_attempts'] >= $max_attempts) {
                                                $_SESSION['lockout_until'] = time() + $lockout_duration;
                                                $is_locked_out = true;
                                                // Klausul 1.5: Pesan warning "Gagal Login"
                                                $pesan_warning = "Gagal Login! Anda telah salah melakukan percobaan login sebanyak 3 kali. Akses login dikunci sementara selama 5 menit.";
                                            } else {
                                                $sisa_percobaan = $max_attempts - $_SESSION['login_attempts'];
                                                $pesan_warning = "Gagal Login! Kode Captcha tidak sesuai. Sisa kesempatan login: $sisa_percobaan kali lagi.";
                                            }
                                        } else {
                                            // CAPTCHA valid, verifikasi kredensial
                                            $usere     = cleankar($_POST['norme']);
                                            $passworde = validPasswordInput($_POST['passworde'], 50);

                                            if (strlen($usere) > 30) {
                                                header('Location: https://www.google.com');
                                                exit();
                                            } else {
                                                // Verifikasi kredensial di database
                                                $login_valid = getOne2("select count(*) from personal_pasien where md5(no_rkm_medis)=md5('$usere') and password=aes_encrypt('$passworde','windi')");

                                                if ($login_valid > 0) {
                                                    // Login berhasil: reset counter percobaan & lockout
                                                    $_SESSION['login_attempts'] = 0;
                                                    unset($_SESSION['lockout_until']);
                                                    $_SESSION["ses_pasien"] = encrypt_decrypt($usere, "e");
                                                    exit(header("Location:index.php"));
                                                } else {
                                                    // Klausul 1.3: Delay time (0.5 detik)
                                                    usleep(500000);

                                                    $_SESSION['login_attempts'] = (isset($_SESSION['login_attempts']) ? (int)$_SESSION['login_attempts'] : 0) + 1;

                                                    // Klausul 1.4: Lock Out setelah 3 kali salah
                                                    if ($_SESSION['login_attempts'] >= $max_attempts) {
                                                        $_SESSION['lockout_until'] = time() + $lockout_duration;
                                                        $is_locked_out = true;
                                                        // Klausul 1.5: Pesan warning "Gagal Login"
                                                        $pesan_warning = "Gagal Login! Anda telah salah melakukan percobaan login sebanyak 3 kali. Akses login dikunci sementara selama 5 menit.";
                                                    } else {
                                                        $sisa_percobaan = $max_attempts - $_SESSION['login_attempts'];
                                                        // Klausul 1.5 & 1.6: Pesan warning persis "Gagal Login" tanpa membedakan username atau password salah
                                                        $pesan_warning = "Gagal Login";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!empty($pesan_warning)) {
                                    echo "<div class=\"col-md-12 col-sm-12\">
                                            <div class=\"alert alert-danger\" style=\"background-color:#f8d7da; border:1px solid #f5c6cb; color:#721c24; padding:12px 15px; border-radius:4px; margin-bottom:18px;\">
                                                <i class=\"fa fa-exclamation-triangle\"></i> <strong>$pesan_warning</strong>
                                            </div>
                                          </div>";
                                }
                            ?>
                            <form id="appointment-form" role="form" onsubmit="return validasiIsi();" method="post" action="index.php?act=LoginPasien" enctype="multipart/form-data">
                                <div class="col-md-12 col-sm-12">
                                    <label for="norme">Nomer Rekam Medis</label>
                                    <input type="password" class="form-control" name="norme" pattern="[A-Z0-9-]{1,65}" title="A-Z0-9- (Maksimal 65 karakter)" required placeholder="Masukkan Nomor Rekam Medis" onkeydown="setDefault(this, document.getElementById('MsgIsi1'));" id="TxtIsi1" autocomplete="off" autofocus <?=($is_locked_out ? 'disabled="disabled"' : '');?>/>
                                    <span id="MsgIsi1" style="color:#CC0000; font-size:10px;"></span>
                                </div>
                                <div class="col-md-12 col-sm-12">
                                    <label for="passworde">Password</label>
                                    <input type="password" class="form-control" name="passworde" required placeholder="Masukkan Password" onkeydown="setDefault(this, document.getElementById('MsgIsi2'));" id="TxtIsi2" autocomplete="off" <?=($is_locked_out ? 'disabled="disabled"' : '');?>/>
                                    <span id="MsgIsi2" style="color:#CC0000; font-size:10px;"></span>
                                </div>
                                <div class="col-md-12 col-sm-12">
                                    <label for="captcha">Captcha</label>
                                    <table width="100%" border="0">
                                        <tr>
                                            <td width="50%" valign="top">
                                                <img id="imgCaptcha" width="95%" height="45px" src="pages/captcha.php?v=<?=time();?>" alt="Captcha Image" style="border-radius:3px; border:1px solid #ddd;" />
                                                <br/>
                                                <small><a href="javascript:void(0)" onclick="document.getElementById('imgCaptcha').src='pages/captcha.php?v=' + new Date().getTime();" style="font-size:11px; color:#06c;"><i class="fa fa-refresh"></i> Ganti Gambar Captcha</a></small>
                                            </td>
                                            <td width="50%">
                                                <input type="text" class="form-control" onkeydown="setDefault(this, document.getElementById('MsgIsi3'));" id="TxtIsi3" name="inputcaptcha" pattern="[0-9]{1,6}" title="0-9 (Maksimal 6 karakter)" required placeholder="Masukkan Captcha" autocomplete="off" <?=($is_locked_out ? 'disabled="disabled"' : '');?>/>
                                            </td>
                                        </tr>
                                    </table>
                                    <span id="MsgIsi3" style="color:#CC0000; font-size:10px;"></span>
                                </div>
                                <div class="col-md-12 col-sm-12" style="margin-top:10px;">
                                    <?php if ($is_locked_out): ?>
                                        <button type="button" class="form-control" id="cf-submit" style="background-color:#999; border-color:#999; cursor:not-allowed;" disabled="disabled">Akses Terkunci (Tunggu <?=$sisa_menit;?> Menit)</button>
                                    <?php else: ?>
                                        <button type="submit" class="form-control" id="cf-submit" name="BtnLogin">Log In</button>
                                    <?php endif; ?>
                                </div>
                            </form>
                        </div>
                    </div>
               </div>
          </div>
     </section>
</body>

    