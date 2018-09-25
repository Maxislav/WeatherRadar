/** @example
 File pass-config.json
 {
   "pass": "glider"
 }
*/

const fs = require('fs');
const exec = require('child_process').exec;

fs.readFile('pass-config.json', (err, data) => {
  if (err) throw err;

  let pass = JSON.parse(data.toString()).pass

  let mess = ''
  process.argv.forEach((val, index, array)=> {
    if (2 <= index) {
      mess += val + ' '
    }
  })

  if (mess.length == 0) {
    console.log("Enter message commit")
    return
  }

  onExec("cd app && C:\\\"Program Files\"\\7-Zip\\7z.exe a build.gradle.zip build.gradle -p"+pass)
    .then(d=> {
    console.log('oloew')
      return onExec("cd app\\src\\main\\ &&  C:\\\"Program Files\"\\7-Zip\\7z.exe" + " a AndroidManifest.xml.zip AndroidManifest.xml -p"+pass)
    })
    .then((d)=> {
      return onExec("git add * && git commit -m \"" + mess + "\" && git push ")
    })

});

function onExec(str) {
  return new Promise((resolve, reject)=> {
    exec(str, (error, stdout, stderr) => {
      if (error) {
        console.error(`exec error: ${error}`);
        reject(error)
        return;
      }
      console.log(`stdout: ${stdout}`);
      console.log(`stderr: ${stderr}`);
      resolve(stdout)
    });
  })

}


