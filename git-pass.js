/**
 * Created by maxislav on 10.08.16.
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

  onExec("cd app/; zip -P \"" + pass + "\" build.gradle.zip -r build.gradle")
    .then(d=> {
      return onExec("cd app/src/main/; zip -P \"" + pass + "\" AndroidManifest.xml.zip -r AndroidManifest.xml")
    })
    .then((d)=> {
      return onExec("git add * ; git commit -m \"" + mess + "\"; git push ")
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


